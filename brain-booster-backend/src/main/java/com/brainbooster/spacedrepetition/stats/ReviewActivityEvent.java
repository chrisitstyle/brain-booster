package com.brainbooster.spacedrepetition.stats;

import com.brainbooster.spacedrepetition.history.ReviewRating;
import com.brainbooster.spacedrepetition.state.ReviewStage;

import java.time.Instant;

/**
 * Contains the review event fields needed for learning analytics.
 *
 * @param reviewedAt timestamp of the submitted rating
 * @param rating submitted FSRS rating
 * @param stageBefore flashcard stage before the rating
 */
public record ReviewActivityEvent(
        Instant reviewedAt,
        ReviewRating rating,
        ReviewStage stageBefore
) {
}
