package com.brainbooster.gameresult.dto;

import com.brainbooster.gameresult.GameMode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response containing a completed game attempt with question-level results.")
public record GameAttemptDTO(

        @Schema(description = "Unique ID of the game attempt.", example = "1")
        Long attemptId,

        @Schema(description = "ID of the user who completed the attempt.", example = "5")
        Long userId,

        @Schema(description = "ID of the related flashcard set.", example = "12")
        Long setId,

        @Schema(description = "Game mode related to the attempt.", example = "multiple-choice")
        GameMode mode,

        @Schema(description = "Number of correctly answered questions.", example = "8")
        Integer score,

        @Schema(description = "Total number of questions in the attempt.", example = "10")
        Integer totalQuestions,

        @Schema(description = "Duration of the attempt in seconds.", example = "120")
        Integer durationSeconds,

        @Schema(description = "Date and time when the attempt was completed.")
        LocalDateTime completedAt,

        @Schema(description = "Question-level results recorded during the attempt.")
        List<GameQuestionResultDTO> questionResults
) {
}
