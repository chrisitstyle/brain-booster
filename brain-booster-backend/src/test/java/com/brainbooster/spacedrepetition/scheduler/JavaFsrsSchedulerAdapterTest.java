
package com.brainbooster.spacedrepetition.scheduler;

import com.brainbooster.spacedrepetition.history.ReviewRating;
import io.github.openspacedrepetition.Card;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Verifies FSRS scheduling, state restoration and input validation.
 *
 * <p>Review timestamps are explicitly supplied to keep tests
 * deterministic and independent of the system clock.
 */
class JavaFsrsSchedulerAdapterTest {

    private static final Instant FIRST_REVIEW_AT = Instant.parse("2026-11-09T12:00:00Z");

    private final FsrsSchedulerAdapter fsrsSchedulerAdapter = new JavaFsrsSchedulerAdapter();

    @ParameterizedTest
    @EnumSource(ReviewRating.class)
    void review_shouldScheduleNextReview_whenCardIsNew(
            ReviewRating reviewRating
    ) {
        // given
        double desiredRetention = 0.90;

        Instant reviewedAt = FIRST_REVIEW_AT;

        // when
        FsrsReviewResult reviewResult = fsrsSchedulerAdapter.review(
                null,
                reviewRating,
                desiredRetention,
                reviewedAt);

        // then
        assertThat(reviewResult.previousCardData()).isNull();

        assertThat(reviewResult.updatedCardData()).isNotBlank();

        assertThat(reviewResult.reviewedAt()).isEqualTo(reviewedAt);

        assertThat(reviewResult.nextReviewAt()).isAfter(reviewedAt);

        Card restoredCard = Card.fromJson(reviewResult.updatedCardData());

        assertThat(restoredCard.getDue()).isEqualTo(reviewResult.nextReviewAt());
    }

    @Test
    void review_shouldRestorePreviousState_whenCardWasReviewedBefore() {
        // given
        FsrsReviewResult firstReview = fsrsSchedulerAdapter.review(
                null,
                ReviewRating.GOOD,
                0.90,
                FIRST_REVIEW_AT);

        // review the card after its previously scheduled due date
        Instant secondReviewAt = firstReview.nextReviewAt()
                .plusSeconds(60);

        // when
        FsrsReviewResult secondReview = fsrsSchedulerAdapter.review(
                firstReview.updatedCardData(),
                ReviewRating.GOOD,
                0.90,
                secondReviewAt);

        // then
        assertThat(secondReview.previousCardData()).isEqualTo(firstReview.updatedCardData());

        assertThat(secondReview.updatedCardData()).isNotBlank();

        assertThat(secondReview.reviewedAt()).isEqualTo(secondReviewAt);

        assertThat(secondReview.nextReviewAt()).isAfter(secondReviewAt);

        Card restoredCard = Card.fromJson(secondReview.updatedCardData());

        assertThat(restoredCard.getDue()).isEqualTo(secondReview.nextReviewAt());
    }

    @Test
    void review_shouldSupportDifferentRetention_whenSettingsAreValid() {
        // given
        double desiredRetention = 0.85;

        Instant reviewedAt = FIRST_REVIEW_AT;

        // when
        FsrsReviewResult reviewResult = fsrsSchedulerAdapter.review(
                null,
                ReviewRating.EASY,
                desiredRetention,
                reviewedAt);

        // then
        assertThat(reviewResult.updatedCardData()).isNotBlank();

        assertThat(reviewResult.reviewedAt()).isEqualTo(reviewedAt);

        assertThat(reviewResult.nextReviewAt()).isAfter(reviewedAt);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 0.0, 1.0, 1.1})
    void review_shouldRejectRetention_whenValueIsOutsideAllowedRange(double desiredRetention) {
        // given
        ReviewRating reviewRating = ReviewRating.GOOD;

        Instant reviewedAt = FIRST_REVIEW_AT;

        // when
        Throwable thrownException = catchThrowable(() ->
                fsrsSchedulerAdapter.review(
                        null,
                        reviewRating,
                        desiredRetention,
                        reviewedAt));
        // then
        assertThat(thrownException)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Desired retention must be between 0 and 1.");
    }

    @Test
    void review_shouldRejectRetention_whenValueIsNotFinite() {
        // given
        double desiredRetention = Double.NaN;

        Instant reviewedAt = FIRST_REVIEW_AT;

        // when
        Throwable thrownException = catchThrowable(() ->
                fsrsSchedulerAdapter.review(null,
                        ReviewRating.GOOD,
                        desiredRetention,
                        reviewedAt));

        // then
        assertThat(thrownException)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Desired retention must be between 0 and 1.");
    }

    @Test
    void review_shouldRejectRating_whenRatingIsNull() {
        // given
        ReviewRating reviewRating = null;

        Instant reviewedAt = FIRST_REVIEW_AT;

        // when
        Throwable thrownException = catchThrowable(() -> fsrsSchedulerAdapter.review(
                null,
                reviewRating,
                0.90,
                reviewedAt));

        // then
        assertThat(thrownException)
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Review rating must not be null.");
    }

    @Test
    void review_shouldRejectTimestamp_whenTimestampIsNull() {
        // given
        Instant reviewedAt = null;

        // when
        Throwable thrownException = catchThrowable(() ->
                fsrsSchedulerAdapter.review(
                        null,
                        ReviewRating.GOOD,
                        0.90,
                        reviewedAt));

        // then
        assertThat(thrownException)
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Review timestamp must not be null.");
    }

    @Test
    void review_shouldRejectCardData_whenSerializedStateIsInvalid() {
        // given
        String invalidCardData = "invalid-json";

        Instant reviewedAt = FIRST_REVIEW_AT;

        // when
        Throwable thrownException = catchThrowable(() -> fsrsSchedulerAdapter.review(
                invalidCardData,
                ReviewRating.GOOD,
                0.90,
                reviewedAt));

        // then
        assertThat(thrownException)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid stored FSRS card data.");
    }
}
