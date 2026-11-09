
package com.brainbooster.spacedrepetition.service;

import com.brainbooster.exception.ReviewConflictException;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLogRepository;
import com.brainbooster.spacedrepetition.settings.UserReviewSettings;
import com.brainbooster.spacedrepetition.settings.UserReviewSettingsRepository;
import com.brainbooster.spacedrepetition.state.ReviewStage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Verifies the daily limit policy and local-day boundary calculations.
 */
@ExtendWith(MockitoExtension.class)
class ReviewDailyLimitServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private FlashcardReviewLogRepository reviewLogRepository;

    @Mock
    private UserReviewSettingsRepository reviewSettingsRepository;

    @InjectMocks
    private ReviewDailyLimitService reviewDailyLimitService;

    @Test
    void getAllowance_shouldReturnDefaultLimits_whenSettingsDoNotExist() {
        // given
        Instant currentTime = Instant.parse("2026-11-09T12:00:00Z");

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        // when
        ReviewDailyAllowance allowance = reviewDailyLimitService.getAllowance(USER_ID, currentTime);

        // then
        assertThat(allowance.remainingNewCards()).isEqualTo(20);
        assertThat(allowance.remainingReviews()).isEqualTo(200);
    }

    @Test
    void getAllowance_shouldSubtractCompletedReviews_fromDailyLimits() {
        // given
        Instant currentTime = Instant.parse("2026-11-09T12:00:00Z");

        UserReviewSettings settings = UserReviewSettings.builder()
                        .newCardsPerDay(20)
                        .reviewLimitPerDay(200)
                        .timezone("UTC")
                        .build();

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.of(settings));

        Instant start = Instant.parse("2026-11-09T00:00:00Z");

        Instant end = Instant.parse("2026-11-10T00:00:00Z");

        when(reviewLogRepository.countByUserStageAndPeriod(
                USER_ID,
                ReviewStage.NEW,
                start,
                end
        )).thenReturn(5L);

        when(reviewLogRepository.countByUserStageAndPeriod(
                USER_ID,
                ReviewStage.REVIEW,
                start,
                end
        )).thenReturn(12L);

        // when
        ReviewDailyAllowance allowance = reviewDailyLimitService.getAllowance(
                        USER_ID,
                        currentTime);

        // then
        assertThat(allowance.remainingNewCards()).isEqualTo(15);
        assertThat(allowance.remainingReviews()).isEqualTo(188);
    }

    @Test
    void getAllowance_shouldUseLocalDay_whenDaylightSavingTimeStarts() {
        // given
        Instant currentTime = Instant.parse("2026-03-29T12:00:00Z");

        UserReviewSettings settings = UserReviewSettings.builder()
                        .timezone("Europe/Warsaw")
                        .build();

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.of(settings));

        // the spring transition makes this local day 23 hours long
        Instant expectedStart = Instant.parse("2026-03-28T23:00:00Z");

        Instant expectedEnd = Instant.parse("2026-03-29T22:00:00Z");

        // when
        reviewDailyLimitService.getAllowance(USER_ID, currentTime);

        // then
        verify(reviewLogRepository).countByUserStageAndPeriod(
                USER_ID,
                ReviewStage.NEW,
                expectedStart,
                expectedEnd);

        verify(reviewLogRepository).countByUserStageAndPeriod(
                USER_ID,
                ReviewStage.REVIEW,
                expectedStart,
                expectedEnd);
    }

    @Test
    void getAllowance_shouldUseLocalDay_whenDaylightSavingTimeEnds() {
        // given
        Instant currentTime = Instant.parse("2026-10-25T12:00:00Z");

        UserReviewSettings settings = UserReviewSettings.builder()
                        .timezone("Europe/Warsaw")
                        .build();

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.of(settings));

        // the autumn transition makes this local day 25 hours long*
        Instant expectedStart = Instant.parse("2026-10-24T22:00:00Z");

        Instant expectedEnd = Instant.parse("2026-10-25T23:00:00Z");

        // when
        reviewDailyLimitService.getAllowance(USER_ID, currentTime);

        // then
        verify(reviewLogRepository).countByUserStageAndPeriod(
                USER_ID,
                ReviewStage.NEW,
                expectedStart,
                expectedEnd);

        verify(reviewLogRepository).countByUserStageAndPeriod(
                USER_ID,
                ReviewStage.REVIEW,
                expectedStart,
                expectedEnd);
    }

    @Test
    void verifyReviewAllowed_shouldRejectNewCard_whenLimitIsReached() {
        // given
        Instant currentTime = Instant.parse("2026-11-09T12:00:00Z");

        UserReviewSettings settings = UserReviewSettings.builder()
                        .newCardsPerDay(0)
                        .build();

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.of(settings));

        // when
        // then
        assertThatThrownBy(() -> reviewDailyLimitService.verifyReviewAllowed(
                        USER_ID,
                        ReviewStage.NEW,
                        currentTime))
                .isInstanceOf(ReviewConflictException.class)
                .hasMessage("Daily new card limit reached.");
    }

    @Test
    void verifyReviewAllowed_shouldRejectReview_whenLimitIsReached() {
        // given
        Instant currentTime = Instant.parse("2026-11-09T12:00:00Z");

        UserReviewSettings settings = UserReviewSettings.builder()
                        .reviewLimitPerDay(0)
                        .build();

        when(reviewSettingsRepository.findById(USER_ID)).thenReturn(Optional.of(settings));

        // when
        // then
        assertThatThrownBy(() -> reviewDailyLimitService.verifyReviewAllowed(
                        USER_ID,
                        ReviewStage.REVIEW,
                        currentTime))
                .isInstanceOf(ReviewConflictException.class)
                .hasMessage("Daily review limit reached.");
    }

    @Test
    void verifyReviewAllowed_shouldAllowLearning_whenLimitsAreReached() {
        // given
        Instant currentTime = Instant.parse("2026-11-09T12:00:00Z");

        // when
        reviewDailyLimitService.verifyReviewAllowed(
                USER_ID,
                ReviewStage.LEARNING,
                currentTime);

        reviewDailyLimitService.verifyReviewAllowed(
                USER_ID,
                ReviewStage.RELEARNING,
                currentTime);

        // then
        verifyNoInteractions(reviewLogRepository, reviewSettingsRepository);
    }

    @Test
    void allowance_shouldConsumeOnlyMatchingLimit_whenReviewIsSelected() {
        // given
        ReviewDailyAllowance allowance = new ReviewDailyAllowance(2, 3);

        // when
        ReviewDailyAllowance afterNewCard = allowance.consume(ReviewStage.NEW);

        ReviewDailyAllowance afterReview = afterNewCard.consume(ReviewStage.REVIEW);

        ReviewDailyAllowance afterLearning = afterReview.consume(ReviewStage.LEARNING);

        // then
        assertThat(afterNewCard)
                .isEqualTo(new ReviewDailyAllowance(1, 3));

        assertThat(afterReview)
                .isEqualTo(new ReviewDailyAllowance(1, 2));

        assertThat(afterLearning).isEqualTo(afterReview);

        assertThat(new ReviewDailyAllowance(0, 0)
                .allows(ReviewStage.LEARNING)).isTrue();

        assertThat(new ReviewDailyAllowance(0, 0)
                .allows(ReviewStage.RELEARNING)).isTrue();

        assertThat(new ReviewDailyAllowance(0, 0)
                .allows(ReviewStage.NEW)).isFalse();

        assertThat(new ReviewDailyAllowance(0, 0)
                .allows(ReviewStage.REVIEW)).isFalse();
    }
}
