package com.brainbooster.spacedrepetition.service;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.exception.ReviewConflictException;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.spacedrepetition.dto.ReviewResultDTO;
import com.brainbooster.spacedrepetition.dto.SubmitReviewRequest;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLog;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLogRepository;
import com.brainbooster.spacedrepetition.history.ReviewRating;
import com.brainbooster.spacedrepetition.scheduler.FsrsReviewResult;
import com.brainbooster.spacedrepetition.scheduler.FsrsSchedulerAdapter;
import com.brainbooster.spacedrepetition.settings.UserReviewSettings;
import com.brainbooster.spacedrepetition.settings.UserReviewSettingsRepository;
import com.brainbooster.spacedrepetition.state.FlashcardReviewState;
import com.brainbooster.spacedrepetition.state.FlashcardReviewStateRepository;
import com.brainbooster.spacedrepetition.state.ReviewStage;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import io.github.openspacedrepetition.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Records flashcard reviews and updates their FSRS scheduling state.
 *
 * <p>Each operation stores the new card state and an immutable review
 * log within one database transaction.
 *
 * <p>A pessimistic user-row lock serializes review submissions for
 * the same user. This also protects the creation of the first state
 * record, which does not yet exist when the transaction starts.
 *
 * <p>Request IDs provide idempotency: repeating an identical request
 * returns its original result without applying the rating twice.
 */
@Service
@RequiredArgsConstructor
public class ReviewSchedulingService {

    private static final String ALGORITHM_VERSION = "FSRS-6";
    private static final double DEFAULT_DESIRED_RETENTION = 0.90;

    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final FlashcardReviewStateRepository reviewStateRepository;
    private final FlashcardReviewLogRepository reviewLogRepository;
    private final UserReviewSettingsRepository reviewSettingsRepository;
    private final FsrsSchedulerAdapter fsrsSchedulerAdapter;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;
    private final ReviewDailyLimitService reviewDailyLimitService;


    /**
     * Applies a rating and persists the resulting FSRS schedule.
     *
     * @param request validated review request
     * @return recorded review, or the original result for an identical retry
     */
    @Transactional
    public ReviewResultDTO submitReview(SubmitReviewRequest request) {
        Long userId = currentUserProvider.getCurrentUser().userId();

        // Lock the user row before checking idempotency or creating a review state.
        // This also serializes daily limit checks for concurrent submissions.
        User user = userRepository.findByIdForReviewUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // return the original result when the same request is submitted again
        FlashcardReviewLog existingLog = reviewLogRepository
                .findByRequestId(request.requestId())
                .orElse(null);

        if (existingLog != null) {
            return resolveExistingRequest(existingLog, userId, request);
        }

        Flashcard flashcard = flashcardRepository
                .findByIdWithSetAndUser(request.flashcardId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Flashcard not found."));

        verifyFlashcardOwnership(flashcard, userId);

        FlashcardReviewState reviewState = reviewStateRepository
                .findByUser_UserIdAndFlashcard_FlashcardId(
                        userId,
                        request.flashcardId())
                .orElse(null);

        Instant currentTime = clock.instant();

        verifyReviewIsDue(reviewState, currentTime);

        ReviewStage previousStage = reviewState == null
                ? ReviewStage.NEW
                : reviewState.getState();

        // Enforce daily limits before calculating the next FSRS schedule.
        // LEARNING and RELEARNING do not consume daily capacity.
        reviewDailyLimitService.verifyReviewAllowed(
                userId,
                previousStage,
                currentTime);

        Instant previousDueAt = reviewState == null
                ? null
                : reviewState.getDueAt();

        String previousCardData = reviewState == null
                ? null
                : reviewState.getFsrsCardData();

        double desiredRetention = reviewSettingsRepository
                .findById(userId)
                .map(UserReviewSettings::getDesiredRetention)
                .orElse(DEFAULT_DESIRED_RETENTION);

        // calculate the next review using the complete FSRS memory state
        FsrsReviewResult fsrsResult = fsrsSchedulerAdapter.review(
                previousCardData,
                request.rating(),
                desiredRetention,
                currentTime);

        // Normalize timestamps to PostgreSQL microsecond precision.
        // This ensures that idempotent retries return consistent timestamps.
        Instant reviewedAt = fsrsResult.reviewedAt()
                .truncatedTo(ChronoUnit.MICROS);

        Instant nextReviewAt = fsrsResult.nextReviewAt()
                .truncatedTo(ChronoUnit.MICROS);

        // Restore the calculated card state instead of inferring its stage
        // directly from the user's rating.
        Card updatedCard = Card.fromJson(fsrsResult.updatedCardData());

        ReviewStage nextStage = ReviewStage.valueOf(updatedCard.getState().name());

        if (reviewState == null) {
            reviewState = FlashcardReviewState.builder()
                    .user(user)
                    .flashcard(flashcard)
                    .build();
        }

        // update the persisted review state with the latest FSRS calculation
        reviewState.setState(nextStage);
        reviewState.setDueAt(nextReviewAt);
        reviewState.setLastReviewedAt(reviewedAt);
        reviewState.setFsrsCardData(fsrsResult.updatedCardData());
        reviewState.setStability(updatedCard.getStability());
        reviewState.setDifficulty(updatedCard.getDifficulty());
        reviewState.setAlgorithmVersion(ALGORITHM_VERSION);

        reviewState.setReviewCount(reviewState.getReviewCount() + 1);

        // count a lapse only when a card in the REVIEW stage is forgotten
        if (previousStage == ReviewStage.REVIEW
                && request.rating() == ReviewRating.AGAIN) {
            reviewState.setLapses(reviewState.getLapses() + 1);
        }

        // Persist the complete FSRS state.
        // The serialized card is the source of truth for future scheduling.
        FlashcardReviewState savedState =
                reviewStateRepository.saveAndFlush(reviewState);

        // store an immutable history entry for the completed review
        FlashcardReviewLog reviewLog = FlashcardReviewLog.builder()
                .reviewState(savedState)
                .rating(request.rating())
                .reviewedAt(reviewedAt)
                .previousDueAt(previousDueAt)
                .nextDueAt(nextReviewAt)
                .stageBefore(previousStage)
                .stageAfter(nextStage)
                .cardDataBefore(previousCardData)
                .cardDataAfter(fsrsResult.updatedCardData())
                .algorithmVersion(ALGORITHM_VERSION)
                .requestId(request.requestId())
                .build();

        reviewLogRepository.saveAndFlush(reviewLog);

        return mapToResultDTO(reviewLog);
    }


    private ReviewResultDTO resolveExistingRequest(
            FlashcardReviewLog existingLog,
            Long userId,
            SubmitReviewRequest request
    ) {
        boolean sameUser = userId.equals(existingLog.getReviewState().getUser().getUserId());

        boolean sameFlashcard = request.flashcardId().equals(
                existingLog.getReviewState()
                        .getFlashcard()
                        .getFlashcardId());

        boolean sameRating = request.rating() == existingLog.getRating();

        if (!sameUser || !sameFlashcard || !sameRating) {
            throw new ReviewConflictException("Request ID has already been used for another review.");
        }

        // replaying an identical request must not call FSRS again
        return mapToResultDTO(existingLog);
    }

    private void verifyFlashcardOwnership(Flashcard flashcard, Long userId) {
        Long ownerId = flashcard.getFlashcardSet()
                .getUser()
                .getUserId();

        if (!userId.equals(ownerId)) {
            throw new AccessDeniedException("You can only review flashcards from your own sets.");
        }
    }

    private void verifyReviewIsDue(
            FlashcardReviewState reviewState,
            Instant currentTime
    ) {
        if (reviewState == null || reviewState.getState() == ReviewStage.NEW) {
            return;
        }

        Instant dueAt = reviewState.getDueAt();

        if (dueAt == null || dueAt.isAfter(currentTime)) {
            throw new ReviewConflictException("Flashcard is not due for review yet.");
        }
    }

    private ReviewResultDTO mapToResultDTO(
            FlashcardReviewLog reviewLog) {
        return new ReviewResultDTO(
                reviewLog.getRequestId(),
                reviewLog.getReviewState()
                        .getFlashcard()
                        .getFlashcardId(),
                reviewLog.getRating(),
                reviewLog.getStageAfter(),
                reviewLog.getReviewedAt(),
                reviewLog.getNextDueAt());
    }
}
