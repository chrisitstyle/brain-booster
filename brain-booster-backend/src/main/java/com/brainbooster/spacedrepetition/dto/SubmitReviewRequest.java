package com.brainbooster.spacedrepetition.dto;

import com.brainbooster.spacedrepetition.history.ReviewRating;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/**
 * Contains the rating submitted for a single flashcard review.
 *
 * <p>The request ID identifies one logical review operation.
 * A client retry must reuse the same ID to avoid duplicate reviews.
 *
 * @param flashcardId identifier of the reviewed flashcard
 * @param rating user's recall rating
 * @param requestId unique identifier of the review operation
 */
public record SubmitReviewRequest(
        @NotNull
        @Positive
        Long flashcardId,

        @NotNull
        ReviewRating rating,

        @NotNull
        UUID requestId) { }
