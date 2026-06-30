package com.brainbooster.gameresult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Flashcard that the authenticated user struggles with.")
public record WeakFlashcardDTO(

        @Schema(description = "Flashcard ID.", example = "25")
        Long flashcardId,

        @Schema(description = "Flashcard term.", example = "cat")
        String term,

        @Schema(description = "Flashcard definition.", example = "kot")
        String definition,

        @Schema(description = "Total answers recorded for this flashcard.", example = "10")
        Long totalAnswers,

        @Schema(description = "Correct answers recorded for this flashcard.", example = "6")
        Long correctAnswers,

        @Schema(description = "Incorrect answers recorded for this flashcard.", example = "4")
        Long incorrectAnswers,

        @Schema(description = "Total mistakes recorded for this flashcard.", example = "5")
        Integer totalMistakes,

        @Schema(description = "Accuracy percentage for this flashcard.", example = "60.0")
        Double accuracyPercentage,

        @Schema(description = "Date and time when this flashcard was last answered.")
        Instant lastAnsweredAt
) {
}
