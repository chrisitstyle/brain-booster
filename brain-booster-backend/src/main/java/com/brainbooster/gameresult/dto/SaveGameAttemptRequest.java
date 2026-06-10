package com.brainbooster.gameresult.dto;

import com.brainbooster.gameresult.GameMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request used to save a completed game attempt with question-level results.")
public record SaveGameAttemptRequest(

        @Schema(
                description = "ID of the study set for which the attempt should be saved.",
                example = "12",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        Long setId,

        @Schema(
                description = "Game mode in which the attempt was completed.",
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
                example = "120",
                minimum = "0"
        )
        @Min(0)
        Integer durationSeconds,

        @Schema(description = "Question-level results recorded during this attempt.")
        @Valid
        List<SaveGameQuestionResultRequest> questionResults
) {
}
