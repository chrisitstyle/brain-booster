
package com.brainbooster.spacedrepetition.service;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.spacedrepetition.dto.ReviewCardDTO;
import com.brainbooster.spacedrepetition.state.FlashcardReviewState;
import com.brainbooster.spacedrepetition.state.FlashcardReviewStateRepository;
import com.brainbooster.spacedrepetition.state.ReviewStage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds FSRS review queues for flashcard sets owned by the current user.
 *
 * <p>Due cards are presented before new cards. Daily capacity is
 * shared across all sets and calculated from persisted review history.
 *
 * <p>LEARNING and RELEARNING cards remain available even when
 * ordinary review limits have been reached.
 */
@Service
@RequiredArgsConstructor
public class ReviewSessionService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardReviewStateRepository reviewStateRepository;
    private final ReviewDailyLimitService reviewDailyLimitService;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    /**
     * Returns due and new flashcards within the user's daily limits.
     *
     * @param setId flashcard set identifier
     * @return ordered cards available for review
     */
    @Transactional(readOnly = true)
    public List<ReviewCardDTO> getReviewQueue(Long setId) {
        Long userId = currentUserProvider.getCurrentUser().userId();

        FlashcardSet flashcardSet = flashcardSetRepository
                .findByIdWithUser(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard set not found."));

        verifySetOwnership(flashcardSet, userId);

        Instant currentTime = clock.instant();

        ReviewDailyAllowance allowance = reviewDailyLimitService.getAllowance(
                        userId,
                        currentTime);

        List<FlashcardReviewState> reviewStates = reviewStateRepository
                        .findByUser_UserIdAndFlashcard_FlashcardSet_SetId(
                                userId,
                                setId);

        Map<Long, FlashcardReviewState> statesByFlashcardId = reviewStates.stream()
                        .collect(Collectors.toMap(
                                state -> state.getFlashcard()
                                        .getFlashcardId(),
                                Function.identity()));

        List<ReviewCardDTO> reviewQueue = new ArrayList<>();

        // prioritize already introduced cards that are currently due
        List<FlashcardReviewState> dueStates = reviewStates.stream()
                .filter(state -> state.getState() != ReviewStage.NEW)
                .filter(state -> state.getDueAt() != null)
                .filter(state ->
                        !state.getDueAt().isAfter(currentTime))
                .sorted(
                        Comparator.comparing(
                                        FlashcardReviewState::getDueAt)
                                .thenComparing(state ->
                                        state.getFlashcard()
                                                .getFlashcardId()))
                .toList();

        for (FlashcardReviewState reviewState : dueStates) {
            ReviewStage stage = reviewState.getState();

            if (!allowance.allows(stage)) {
                continue;
            }

            Flashcard flashcard = reviewState.getFlashcard();

            reviewQueue.add(
                    mapToReviewCard(
                            flashcard,
                            stage,
                            reviewState.getDueAt()));

            allowance = allowance.consume(stage);
        }

        // introduce new cards only while new-card capacity remains
        List<Flashcard> flashcards = flashcardRepository.findAllByFlashcardSet_SetId(setId);

        flashcards.sort(Comparator.comparing(Flashcard::getFlashcardId));

        for (Flashcard flashcard : flashcards) {
            if (!allowance.allows(ReviewStage.NEW)) {
                break;
            }

            FlashcardReviewState reviewState = statesByFlashcardId.get(flashcard.getFlashcardId());

            if (reviewState != null
                    && reviewState.getState() != ReviewStage.NEW)
            {
                continue;
            }

            reviewQueue.add(mapToReviewCard(
                            flashcard,
                            ReviewStage.NEW,
                            null));

            allowance = allowance.consume(ReviewStage.NEW);
        }

        return reviewQueue;
    }

    private void verifySetOwnership(
            FlashcardSet flashcardSet,
            Long userId
    ) {
        Long ownerId = flashcardSet.getUser().getUserId();

        if (!userId.equals(ownerId)) {
            throw new AccessDeniedException("You can only review flashcards from your own sets.");
        }
    }

    private ReviewCardDTO mapToReviewCard(
            Flashcard flashcard,
            ReviewStage stage,
            Instant dueAt
    ) {
        return new ReviewCardDTO(
                flashcard.getFlashcardId(),
                flashcard.getTerm(),
                flashcard.getDefinition(),
                stage,
                dueAt);
    }
}
