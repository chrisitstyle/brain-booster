package com.brainbooster.flashcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body used to create a new flashcard")
public record FlashcardCreationDTO(

        @Schema(
                description = "ID of the flashcard set where the flashcard should be added",
                example = "1"
        )
        @NotNull(message = "setId cannot be empty")
        Long setId,

        @Schema(
                description = "Flashcard term or question",
                example = "to go"
        )
        @NotBlank(message = "term cannot be empty")
        String term,

        @Schema(
                description = "Flashcard definition or answer",
                example = "went - gone"
        )
        @NotBlank(message = "definition cannot be empty")
        String definition
) {
}
