package com.brainbooster.gameresult.dto;

import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.QuestionAnswerSide;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request containing a single question-level result from a completed game attempt.")
public record SaveGameQuestionResultRequest(

        @Schema(
                description = "ID of the flashcard used in this question.",
                example = "25",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        Long flashcardId,

        @Schema(
                description = "Client-side question key used to group related question rows.",
                example = "matching-25-3"
        )
        @Size(max = 120)
        String questionKey,

        @Schema(
                description = "Position of the question inside the attempt.",
                example = "0",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Min(0)
        Integer questionOrder,

        @Schema(
                description = "Type of question.",
                example = "multiple-choice",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        GameQuestionType questionType,

        @Schema(
                description = "Expected answer side.",
                example = "definition"
        )
        QuestionAnswerSide answerWith,

        @Schema(description = "Prompt shown to the user.")
        String prompt,

        @Schema(description = "Answer provided by the user.")
        String userAnswer,

        @Schema(description = "Correct answer for the question.")
        String correctAnswer,

        @Schema(
                description = "Whether the user answered correctly.",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        Boolean wasCorrect,

        @Schema(
                description = "Number of mistakes made for this question.",
                example = "0",
                minimum = "0"
        )
        @Min(0)
        Integer mistakesCount
) {
}
