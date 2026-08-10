package com.brainbooster.flashcardset.dto;

import com.brainbooster.flashcard.dto.FlashcardContentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

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
        String description,
        @Schema(
                description = "Flashcards included in the new set"
        )
        @NotEmpty(message = "Flashcards cannot be empty")
        @Valid
        List<FlashcardContentDTO> flashcards
) {
}
