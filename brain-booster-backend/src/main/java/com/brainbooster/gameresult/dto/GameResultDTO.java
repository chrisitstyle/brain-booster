package com.brainbooster.gameresult.dto;

import com.brainbooster.gameresult.GameMode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response containing the latest saved game result.")
public record GameResultDTO(

        @Schema(description = "Unique ID of the saved game result.", example = "1")
        Long resultId,

        @Schema(description = "ID of the user who owns this result.", example = "5")
        Long userId,

        @Schema(description = "ID of the study set related to this result.", example = "12")
        Long setId,

        @Schema(description = "Game mode related to this result.", example = "multiple-choice")
        GameMode mode,

        @Schema(description = "Number of correctly answered questions.", example = "8")
        Integer score,

        @Schema(description = "Total number of questions.", example = "10")
        Integer totalQuestions,

        @Schema(description = "Duration of the game attempt in seconds.", example = "132")
        Integer durationSeconds,

        @Schema(description = "Date and time when the result was last completed or updated.")
        Instant completedAt
) {
}
