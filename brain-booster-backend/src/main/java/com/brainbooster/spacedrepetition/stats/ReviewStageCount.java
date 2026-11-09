package com.brainbooster.spacedrepetition.stats;

import com.brainbooster.spacedrepetition.state.ReviewStage;

/**
 * Represents an aggregated number of review states in one FSRS stage.
 *
 * @param stage FSRS learning stage
 * @param count number of cards in the stage
 */
public record ReviewStageCount(
        ReviewStage stage,
        long count
) {
}
