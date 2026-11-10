package com.brainbooster.spacedrepetition.dto;

import jakarta.validation.constraints.*;

/**
 * Represents a partial update of spaced repetition preferences.
 *
 * <p>Null fields are ignored. The timezone is supplied automatically
 * by the frontend rather than selected manually by the user.
 *
 * @param desiredRetention requested recall probability
 * @param newCardsPerDay daily limit for new flashcards
 * @param reviewLimitPerDay daily limit for existing-card reviews
 * @param timezone detected IANA time zone
 */
public record UpdateUserReviewSettingsRequest(
        @DecimalMin("0.70")
        @DecimalMax("0.97")
        Double desiredRetention,

        @Min(0)
        @Max(10000)
        Integer newCardsPerDay,

        @Min(0)
        @Max(10000)
        Integer reviewLimitPerDay,

        @Size(max = 64)
        String timezone
) {
}
