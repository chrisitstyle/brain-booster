
package com.brainbooster.spacedrepetition.service;

import com.brainbooster.exception.ReviewConflictException;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLogRepository;
import com.brainbooster.spacedrepetition.settings.UserReviewSettings;
import com.brainbooster.spacedrepetition.settings.UserReviewSettingsRepository;
import com.brainbooster.spacedrepetition.state.ReviewStage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Calculates and enforces global daily FSRS review limits.
 *
 * <p>Daily boundaries are calculated in the user's configured time zone.
 * Review usage is derived from immutable review logs across all sets.
 *
 * <p>Callers performing review submissions must hold the pessimistic
 * user-row lock before checking the allowance.
 */
@Service
@RequiredArgsConstructor
public class ReviewDailyLimitService {

    private final FlashcardReviewLogRepository reviewLogRepository;
    private final UserReviewSettingsRepository reviewSettingsRepository;

    /**
     * Returns the user's remaining review capacity for the local day.
     *
     * @param userId authenticated user identifier
     * @param currentTime current instant
     * @return remaining daily allowance
     */
    public ReviewDailyAllowance getAllowance(
            Long userId,
            Instant currentTime
    ) {
        UserReviewSettings settings = reviewSettingsRepository
                .findById(userId)
                .orElseGet(() -> UserReviewSettings.builder().build());

        ZoneId timezone = ZoneId.of(settings.getTimezone());

        LocalDate currentDate = currentTime
                .atZone(timezone)
                .toLocalDate();

        Instant startInclusive = currentDate
                .atStartOfDay(timezone)
                .toInstant();

        Instant endExclusive = currentDate
                .plusDays(1)
                .atStartOfDay(timezone)
                .toInstant();

        long newCardsStudiedToday = reviewLogRepository.countByUserStageAndPeriod(
                        userId,
                        ReviewStage.NEW,
                        startInclusive,
                        endExclusive);

        long reviewsCompletedToday = reviewLogRepository.countByUserStageAndPeriod(
                        userId,
                        ReviewStage.REVIEW,
                        startInclusive,
                        endExclusive);

        long remainingNewCards = Math.max(
                0L,
                (long) settings.getNewCardsPerDay() - newCardsStudiedToday);

        long remainingReviews = Math.max(
                0L,
                (long) settings.getReviewLimitPerDay() - reviewsCompletedToday);

        return new ReviewDailyAllowance(
                remainingNewCards,
                remainingReviews);
    }

    /**
     * Rejects a rating when its daily limit has been reached.
     *
     * <p>LEARNING and RELEARNING ratings remain available regardless
     * of daily usage.
     *
     * @param userId authenticated user identifier
     * @param stage flashcard stage before the rating
     * @param currentTime current instant
     */
    public void verifyReviewAllowed(
            Long userId,
            ReviewStage stage,
            Instant currentTime
    ) {
        if (stage == ReviewStage.LEARNING
                || stage == ReviewStage.RELEARNING) {
            return;
        }

        ReviewDailyAllowance allowance = getAllowance(userId, currentTime);

        if (allowance.allows(stage)) {
            return;
        }

        if (stage == ReviewStage.NEW) {
            throw new ReviewConflictException("Daily new card limit reached.");
        }

        throw new ReviewConflictException("Daily review limit reached.");
    }
}
