package com.brainbooster.spacedrepetition.dto;

/**
 * Represents the authenticated user's spaced repetition preferences.
 *
 * @param desiredRetention target recall probability
 * @param newCardsPerDay maximum number of new cards per day
 * @param reviewLimitPerDay maximum number of existing-card reviews per day
 * @param timezone IANA time zone used to calculate daily limits
 */
public record UserReviewSettingsDTO(
        double desiredRetention,
        int newCardsPerDay,
        int reviewLimitPerDay,
        String timezone
) {
}
