package com.brainbooster.spacedrepetition.scheduler;

import com.brainbooster.spacedrepetition.history.ReviewRating;

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
     * Calculates the next review after the user rates a flashcard.
     *
     * @param cardData serialized FSRS card state, or null for a new card
     * @param rating the user's recall rating
     * @param desiredRetention target probability of remembering the card
     * @return the updated FSRS card and review timestamps
     */
    FsrsReviewResult review(
            String cardData,
            ReviewRating rating,
            double desiredRetention);
}
