
package com.brainbooster.spacedrepetition.scheduler;

import com.brainbooster.spacedrepetition.history.ReviewRating;
import io.github.openspacedrepetition.Card;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JavaFsrsSchedulerAdapterTest {

    private final FsrsSchedulerAdapter fsrsSchedulerAdapter = new JavaFsrsSchedulerAdapter();

    @ParameterizedTest
    @EnumSource(ReviewRating.class)
    void review_shouldScheduleNextReview_whenCardIsNew(
            ReviewRating reviewRating
    ) {
        // given
        double desiredRetention = 0.90;

        // when
        FsrsReviewResult reviewResult = fsrsSchedulerAdapter.review(
                        null,
                        reviewRating,
                        desiredRetention);

        // then
        assertThat(reviewResult.previousCardData()).isNull();

        assertThat(reviewResult.updatedCardData()).isNotBlank();

        assertThat(reviewResult.reviewedAt()).isNotNull();

        assertThat(reviewResult.nextReviewAt()).isAfter(reviewResult.reviewedAt());
    }

    @Test
    void review_shouldRestorePreviousState_whenCardWasReviewedBefore() {
        // given
        FsrsReviewResult firstReview = fsrsSchedulerAdapter.review(
                        null,
                        ReviewRating.GOOD,
                        0.90);

        // when
        FsrsReviewResult secondReview = fsrsSchedulerAdapter.review(
                        firstReview.updatedCardData(),
                        ReviewRating.GOOD,
                        0.90);

        // then
        assertThat(secondReview.previousCardData()).isEqualTo(firstReview.updatedCardData());

        assertThat(secondReview.updatedCardData()).isNotBlank();

        Card restoredCard = Card.fromJson(secondReview.updatedCardData());

        assertThat(restoredCard.getDue())
                .isEqualTo(secondReview.nextReviewAt());

        assertThat(secondReview.reviewedAt()).isAfterOrEqualTo(firstReview.reviewedAt());
    }

    @Test
    void review_shouldSupportDifferentRetention_whenSettingsAreValid() {
        // given
        double desiredRetention = 0.85;

        // when
        FsrsReviewResult reviewResult = fsrsSchedulerAdapter.review(
                        null,
                        ReviewRating.EASY,
                        desiredRetention);

        // then
        assertThat(reviewResult.updatedCardData()).isNotBlank();

        assertThat(reviewResult.nextReviewAt()).isAfter(reviewResult.reviewedAt());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 0.0, 1.0, 1.1})
    void review_shouldRejectRetention_whenValueIsOutsideAllowedRange(
            double desiredRetention
    ) {
        // given
        ReviewRating reviewRating = ReviewRating.GOOD;

        // when
        // then
        assertThatThrownBy(() -> fsrsSchedulerAdapter.review(
                        null,
                        reviewRating,
                        desiredRetention))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Desired retention must be between 0 and 1.");
    }

    @Test
    void review_shouldRejectRetention_whenValueIsNotFinite() {
        // given
        double desiredRetention = Double.NaN;

        // when
        // then
        assertThatThrownBy(() -> fsrsSchedulerAdapter.review(
                        null,
                        ReviewRating.GOOD,
                        desiredRetention))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Desired retention must be between 0 and 1.");
    }

    @Test
    void review_shouldRejectRating_whenRatingIsNull() {
        // given
        ReviewRating reviewRating = null;

        // when
        // then
        assertThatThrownBy(() -> fsrsSchedulerAdapter.review(
                        null,
                        reviewRating,
                        0.90)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Review rating must not be null.");
    }

    @Test
    void review_shouldRejectCardData_whenSerializedStateIsInvalid() {
        // given
        String invalidCardData = "invalid-json";

        // when
        // then
        assertThatThrownBy(() -> fsrsSchedulerAdapter.review(
                        invalidCardData,
                        ReviewRating.GOOD,
                        0.90))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid stored FSRS card data.");
    }
}
