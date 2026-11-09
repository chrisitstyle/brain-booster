package com.brainbooster.spacedrepetition.dto;

import com.brainbooster.spacedrepetition.history.ReviewRating;
import com.brainbooster.spacedrepetition.state.ReviewStage;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a successfully recorded flashcard review.
 *
 * @param requestId identifier used for idempotent retries
 * @param flashcardId reviewed flashcard identifier
 * @param rating submitted rating
 * @param stage updated FSRS learning stage
 * @param reviewedAt actual review timestamp
 * @param nextReviewAt scheduled timestamp of the next review
 */
public record ReviewResultDTO(
        UUID requestId,
        Long flashcardId,
        ReviewRating rating,
        ReviewStage stage,
        Instant reviewedAt,
        Instant nextReviewAt
) {
}
