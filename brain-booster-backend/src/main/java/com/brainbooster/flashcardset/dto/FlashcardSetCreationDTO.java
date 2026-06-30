package com.brainbooster.flashcardset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body used to create a new flashcard set")
public record FlashcardSetCreationDTO(

        @Schema(
                description = "Flashcard set name",
                example = "English irregular verbs"
        )
        @NotBlank(message = "Set name cannot be empty")
        String setName,

        @Schema(
                description = "Flashcard set description",
                example = "A set for learning the most common English irregular verbs"
        )
        @NotBlank(message = "Description cannot be empty")
        String description
) {
}
