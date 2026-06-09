package com.brainbooster.gameresult;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request used to save or update the latest game result for a study set and game mode.")
public record SaveGameResultRequest(

        @Schema(
                description = "ID of the study set for which the result should be saved.",
                example = "12",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        Long setId,

        @Schema(
                description = "Game mode for which the result was achieved.",
                example = "multiple-choice",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        GameMode mode,

        @Schema(
                description = "Number of correctly answered questions.",
                example = "8",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Min(0)
        Integer score,

        @Schema(
                description = "Total number of questions in the game attempt.",
                example = "10",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Min(1)
        Integer totalQuestions,

        @Schema(
                description = "Optional duration of the completed game attempt in seconds.",
                example = "132",
                minimum = "0"
        )
        @Min(0)
        Integer durationSeconds
) {
}
