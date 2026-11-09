package com.brainbooster.spacedrepetition.service;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.spacedrepetition.dto.ReviewCardDTO;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLogRepository;
import com.brainbooster.spacedrepetition.settings.UserReviewSettings;
import com.brainbooster.spacedrepetition.settings.UserReviewSettingsRepository;
import com.brainbooster.spacedrepetition.state.FlashcardReviewState;
import com.brainbooster.spacedrepetition.state.FlashcardReviewStateRepository;
import com.brainbooster.spacedrepetition.state.ReviewStage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the spaced repetition queue for an authenticated user.
 *
 * <p>Due cards are returned first, followed by unseen cards.
 * Daily limits are calculated using the user's configured timezone
 * and the review history already persisted in PostgreSQL.
 *
 * <p>Reading the queue never creates or modifies FSRS card states.
 */
@Service
@RequiredArgsConstructor
public class ReviewSessionService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardReviewStateRepository reviewStateRepository;
    private final FlashcardReviewLogRepository reviewLogRepository;
    private final UserReviewSettingsRepository reviewSettingsRepository;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    /**
     * Returns cards available for review within the daily study limits.
     *
     * @param setId identifier of the flashcard set
     * @return due cards followed by new cards
     */
    @Transactional(readOnly = true)
    public List<ReviewCardDTO> getReviewQueue(Long setId) {
        Long userId = currentUserProvider.getCurrentUser().userId();

        FlashcardSet flashcardSet = flashcardSetRepository
                .findByIdWithUser(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard set not found."));

        verifySetOwnership(flashcardSet, userId);

        UserReviewSettings settings = reviewSettingsRepository
                .findById(userId)
                .orElseGet(() -> UserReviewSettings.builder().build());

        Instant currentTime = clock.instant();

        ZoneId userTimezone = ZoneId.of(settings.getTimezone());

        LocalDate currentStudyDate = LocalDate.ofInstant(currentTime, userTimezone);

        // Derive day boundaries in the user's timezone, then convert
        // them to UTC instants for database queries.
        Instant dayStart = currentStudyDate
                .atStartOfDay(userTimezone)
                .toInstant();

        Instant dayEnd = currentStudyDate
                .plusDays(1)
                .atStartOfDay(userTimezone)
                .toInstant();

        long completedNewCards =
                reviewLogRepository.countNewCardsReviewedDuringDay(
                        userId,
                        ReviewStage.NEW,
                        dayStart,
                        dayEnd);

        long completedExistingReviews = reviewLogRepository.countExistingCardsReviewedDuringDay(
                        userId,
                        ReviewStage.NEW,
                        dayStart,
                        dayEnd);

        int remainingNewCards = calculateRemainingLimit(
                settings.getNewCardsPerDay(),
                completedNewCards);

        int remainingExistingReviews = calculateRemainingLimit(
                settings.getReviewLimitPerDay(),
                completedExistingReviews);

        List<ReviewCardDTO> reviewQueue = new ArrayList<>();

        if (remainingExistingReviews > 0) {
            List<FlashcardReviewState> dueStates = reviewStateRepository
                            .findByUser_UserIdAndFlashcard_FlashcardSet_SetIdAndDueAtLessThanEqualOrderByDueAtAsc(
                                    userId,
                                    setId,
                                    currentTime,
                                    PageRequest.of(
                                            0,
                                            remainingExistingReviews));

            dueStates.stream()
                    .map(this::mapReviewStateToCard)
                    .forEach(reviewQueue::add);
        }

        if (remainingNewCards > 0) {
            List<Flashcard> newFlashcards = flashcardRepository.findUnreviewedBySetIdAndUserId(
                            setId,
                            userId,
                            PageRequest.of(0, remainingNewCards));

            newFlashcards.stream()
                    .map(this::mapNewFlashcardToCard)
                    .forEach(reviewQueue::add);
        }

        return List.copyOf(reviewQueue);
    }

    private int calculateRemainingLimit(
            int dailyLimit,
            long completedToday
    ) {
        return (int) Math.max(0L, dailyLimit - completedToday);
    }

    private ReviewCardDTO mapReviewStateToCard(
            FlashcardReviewState reviewState
    ) {
        Flashcard flashcard = reviewState.getFlashcard();

        return new ReviewCardDTO(
                flashcard.getFlashcardId(),
                flashcard.getTerm(),
                flashcard.getDefinition(),
                reviewState.getState(),
                reviewState.getDueAt());
    }

    private ReviewCardDTO mapNewFlashcardToCard(
            Flashcard flashcard
    ) {
        return new ReviewCardDTO(
                flashcard.getFlashcardId(),
                flashcard.getTerm(),
                flashcard.getDefinition(),
                ReviewStage.NEW,
                null);
    }

    private void verifySetOwnership(
            FlashcardSet flashcardSet,
            Long userId
    ) {
        Long ownerId = flashcardSet.getUser().getUserId();

        if (!userId.equals(ownerId)) {
            throw new AccessDeniedException("You can only review your own flashcard sets.");
        }
    }
}
