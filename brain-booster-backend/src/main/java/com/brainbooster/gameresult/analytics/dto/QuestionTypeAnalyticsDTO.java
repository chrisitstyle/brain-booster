package com.brainbooster.gameresult.analytics.dto;

import com.brainbooster.gameresult.GameQuestionType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Accuracy statistics grouped by question type.")
public record QuestionTypeAnalyticsDTO(

        @Schema(description = "Question type.", example = "multiple-choice")
        GameQuestionType questionType,

        @Schema(description = "Total answers recorded for this question type.", example = "25")
        Long totalAnswers,

        @Schema(description = "Correct answers recorded for this question type.", example = "20")
        Long correctAnswers,

        @Schema(description = "Incorrect answers recorded for this question type.", example = "5")
        Long incorrectAnswers,

        @Schema(description = "Accuracy percentage for this question type.", example = "80.0")
        Double accuracyPercentage
) {
}
