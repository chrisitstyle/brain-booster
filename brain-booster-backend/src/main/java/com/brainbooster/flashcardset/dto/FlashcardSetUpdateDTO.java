package com.brainbooster.flashcardset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body used to update an existing flashcard set")
public record FlashcardSetUpdateDTO(

        @Schema(
                description = "Updated flashcard set name",
                example = "Advanced English irregular verbs"
        )
        @NotBlank(message = "Set name cannot be empty")
        String setName,

        @Schema(
                description = "Updated flashcard set description",
                example = "Updated set for learning advanced English irregular verbs"
        )
        @NotBlank(message = "Description cannot be empty")
        String description
) {
}

