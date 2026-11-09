package com.brainbooster.spacedrepetition.dto;

import com.brainbooster.spacedrepetition.history.ReviewRating;

import java.time.LocalDate;
import java.util.List;

/**
 * Contains the authenticated user's FSRS learning statistics.
 *
 * @param totalCards number of owned flashcards
 * @param newCards number of flashcards not yet introduced
 * @param learningCards cards in the LEARNING stage
 * @param reviewCards cards in the REVIEW stage
 * @param relearningCards cards in the RELEARNING stage
 * @param dueNow cards currently due, including overdue cards
 * @param reviewedToday ratings submitted on the current local day
 * @param timezone user's configured IANA time zone
 * @param activity review activity for the last 14 local days
 * @param ratings rating distribution for the last 14 local days
 * @param forecast scheduled reviews for the next seven local days
 */
public record ReviewDashboardDTO(
        long totalCards,
        long newCards,
        long learningCards,
        long reviewCards,
        long relearningCards,
        long dueNow,
        long reviewedToday,
        String timezone,
        List<DailyActivity> activity,
        List<RatingCount> ratings,
        List<DailyForecast> forecast
) {

    /**
     * Represents review activity on one local calendar day.
     *
     * @param date local calendar date
     * @param reviews total submitted ratings
     * @param newCards number of newly introduced cards
     */
    public record DailyActivity(
            LocalDate date,
            long reviews,
            long newCards
    ) {
    }

    /**
     * Represents the frequency of one review rating.
     *
     * @param rating FSRS review rating
     * @param count number of submitted ratings
     */
    public record RatingCount(
            ReviewRating rating,
            long count
    ) {
    }

    /**
     * Represents the number of scheduled reviews for a local day.
     *
     * @param date local calendar date
     * @param dueCards number of flashcards due on that day
     */
    public record DailyForecast(
            LocalDate date,
            long dueCards) {
    }
}
