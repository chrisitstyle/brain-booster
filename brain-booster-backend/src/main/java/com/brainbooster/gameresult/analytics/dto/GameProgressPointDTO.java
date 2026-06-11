package com.brainbooster.gameresult.analytics.dto;

import com.brainbooster.gameresult.GameMode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Single progress point based on a completed game attempt.")
public record GameProgressPointDTO(

        @Schema(description = "Date and time when the attempt was completed.")
        LocalDateTime completedAt,

        @Schema(description = "Number of correctly answered questions.", example = "8")
        Integer score,

        @Schema(description = "Total number of questions in the attempt.", example = "10")
        Integer totalQuestions,

        @Schema(description = "Score percentage for this attempt.", example = "80.0")
        Double percentage,

        @Schema(description = "Attempt duration in seconds.", example = "120")
        Integer durationSeconds,

        @Schema(description = "Game mode used in the attempt.", example = "multiple-choice")
        GameMode mode
) {
}
