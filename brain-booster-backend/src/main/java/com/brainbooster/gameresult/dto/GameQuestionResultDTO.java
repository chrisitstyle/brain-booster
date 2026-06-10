package com.brainbooster.gameresult.dto;

import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.QuestionAnswerSide;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response containing a single question result from a game attempt.")
public record GameQuestionResultDTO(

        @Schema(description = "Unique ID of the question result.", example = "1")
        Long questionResultId,

        @Schema(description = "ID of the related game attempt.", example = "10")
        Long attemptId,

        @Schema(description = "ID of the related flashcard.", example = "25")
        Long flashcardId,

        @Schema(description = "Client-side question key used to group related question rows.", example = "matching-25-3")
        String questionKey,

        @Schema(description = "Position of the question inside the attempt.", example = "0")
        Integer questionOrder,

        @Schema(description = "Type of question.", example = "multiple-choice")
        GameQuestionType questionType,

        @Schema(description = "Expected answer side.", example = "definition")
        QuestionAnswerSide answerWith,

        @Schema(description = "Prompt shown to the user.")
        String prompt,

        @Schema(description = "Answer provided by the user.")
        String userAnswer,

        @Schema(description = "Correct answer for the question.")
        String correctAnswer,

        @Schema(description = "Whether the user answered correctly.", example = "true")
        Boolean wasCorrect,

        @Schema(description = "Number of mistakes made for this question.", example = "0")
        Integer mistakesCount,

        @Schema(description = "Date and time when the answer was recorded.")
        LocalDateTime answeredAt
) {
}
