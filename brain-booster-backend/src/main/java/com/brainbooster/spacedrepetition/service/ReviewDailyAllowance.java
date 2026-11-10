
package com.brainbooster.spacedrepetition.service;

import com.brainbooster.spacedrepetition.state.ReviewStage;

/**
 * Represents the remaining daily review capacity for a user.
 *
 * <p>Learning and relearning steps are not limited, allowing users
 * to complete short-term learning sequences.
 *
 * @param remainingNewCards number of new cards that may be started
 * @param remainingReviews number of REVIEW-stage ratings remaining
 */
public record ReviewDailyAllowance(
        long remainingNewCards,
        long remainingReviews
) {

    /**
     * Checks whether the given review stage is allowed.
     *
     * @param stage flashcard stage before the rating
     * @return whether a rating may be submitted
     */
    public boolean allows(ReviewStage stage) {
        return switch (stage) {
            case NEW -> remainingNewCards > 0;
            case REVIEW -> remainingReviews > 0;
            case LEARNING, RELEARNING -> true;
        };
    }

    /**
     * Returns the allowance after reserving a slot for a review.
     *
     * @param stage flashcard stage before the rating
     * @return updated daily allowance
     */
    public ReviewDailyAllowance consume(ReviewStage stage) {
        return switch (stage) {
            case NEW -> new ReviewDailyAllowance(
                    Math.max(0, remainingNewCards - 1),
                    remainingReviews);
            case REVIEW -> new ReviewDailyAllowance(
                    remainingNewCards,
                    Math.max(0, remainingReviews - 1));
            case LEARNING, RELEARNING -> this;
        };
    }
}
