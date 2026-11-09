
package com.brainbooster.spacedrepetition.scheduler;

import com.brainbooster.spacedrepetition.history.ReviewRating;
import io.github.openspacedrepetition.Card;
import io.github.openspacedrepetition.CardAndReviewLog;
import io.github.openspacedrepetition.Rating;
import io.github.openspacedrepetition.Scheduler;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Integrates the Java-FSRS scheduling library with Brain Booster.
 *
 * <p>Restores the complete memory state of a flashcard, applies the
 * user's rating and returns the updated state for persistence.
 *
 * <p>This component does not access the database. Review state updates
 * and review history must be persisted together by the application
 * service within a transaction.
 */
@Component
public class JavaFsrsSchedulerAdapter implements FsrsSchedulerAdapter {

    @Override
    public FsrsReviewResult review(
            String cardData,
            ReviewRating rating,
            double desiredRetention
    ) {
        Objects.requireNonNull(rating, "Review rating must not be null.");

        validateDesiredRetention(desiredRetention);

        Card card = restoreCard(cardData);

        Scheduler scheduler = Scheduler.builder()
                .desiredRetention(desiredRetention)
                .build();

        Rating fsrsRating = Rating.valueOf(rating.name());

        CardAndReviewLog reviewResult = scheduler.reviewCard(card, fsrsRating);

        Card updatedCard = reviewResult.card();

        // Store the complete FSRS state instead of reconstructing
        // it from a subset of database columns.
        String updatedCardData = updatedCard.toJson();

        return new FsrsReviewResult(
                cardData,
                updatedCardData,
                reviewResult.reviewLog().reviewDatetime(),
                updatedCard.getDue());
    }

    private Card restoreCard(String cardData) {
        if (cardData == null || cardData.isBlank()) {
            // a new flashcard has no persisted FSRS memory state
            return Card.builder().build();
        }

        try {
            return Card.fromJson(cardData);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid stored FSRS card data.", exception);
        }
    }

    private void validateDesiredRetention(double desiredRetention) {
        if (!Double.isFinite(desiredRetention)
                || desiredRetention <= 0.0
                || desiredRetention >= 1.0) {
            throw new IllegalArgumentException("Desired retention must be between 0 and 1.");
        }
    }
}
