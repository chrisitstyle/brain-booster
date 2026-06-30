package com.brainbooster.gameresult.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Summary statistics for game attempts in a flashcard set.")
public record GameAnalyticsSummaryDTO(

        @Schema(description = "Total number of completed attempts.", example = "12")
        Long totalAttempts,

        @Schema(description = "Average raw score across attempts.", example = "7.5")
        Double averageScore,

        @Schema(description = "Best raw score achieved in a single attempt.", example = "10")
        Integer bestScore,

        @Schema(description = "Average attempt duration in seconds.", example = "95.25")
        Double averageDuration,

        @Schema(description = "Date and time of the latest completed attempt.")
        Instant lastAttemptAt,

        @Schema(description = "Overall accuracy percentage across all attempts.", example = "82.35")
        Double accuracyPercentage
) {
}
