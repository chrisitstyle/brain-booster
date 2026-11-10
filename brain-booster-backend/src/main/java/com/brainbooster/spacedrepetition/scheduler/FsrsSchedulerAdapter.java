package com.brainbooster.spacedrepetition.scheduler;

import com.brainbooster.spacedrepetition.history.ReviewRating;

import java.time.Instant;

/**
 * Defines the contract for scheduling flashcard reviews using FSRS.
 *
 * <p>Isolates the application from the underlying scheduling library.
 * The persistence layer stores the serialized card state returned by
 * this adapter, allowing subsequent reviews to reuse the full FSRS
 * memory state.
 */
public interface FsrsSchedulerAdapter {

    /**
     * Calculates the next review using the provided timestamp.
     *
     * @param cardData serialized FSRS card state, or null for a new card
     * @param rating user's recall rating
     * @param desiredRetention target probability of remembering the card
     * @param reviewedAt exact timestamp of the review
     * @return the updated FSRS state and scheduling information
     */
    FsrsReviewResult review(
            String cardData,
            ReviewRating rating,
            double desiredRetention,
            Instant reviewedAt);
}
