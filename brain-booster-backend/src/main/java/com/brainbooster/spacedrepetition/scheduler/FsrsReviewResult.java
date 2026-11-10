package com.brainbooster.spacedrepetition.scheduler;

import java.time.Instant;

/**
 * Represents the result of scheduling a flashcard review.
 *
 * @param previousCardData serialized state before the review, or null for a new card
 * @param updatedCardData complete serialized state after the review
 * @param reviewedAt timestamp assigned to the review by FSRS
 * @param nextReviewAt timestamp of the next scheduled review
 */
public record FsrsReviewResult(
        String previousCardData,
        String updatedCardData,
        Instant reviewedAt,
        Instant nextReviewAt) { }
