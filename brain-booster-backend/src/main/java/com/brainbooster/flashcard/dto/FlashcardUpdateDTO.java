package com.brainbooster.flashcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body used to update an existing flashcard")
public record FlashcardUpdateDTO(

        @Schema(
                description = "Updated flashcard term or question",
                example = "to be"
        )
        @NotBlank(message = "Term cannot be empty")
        String term,

        @Schema(
                description = "Updated flashcard definition or answer",
                example = "was / were - been"
        )
        @NotBlank(message = "Definition cannot be empty")
        String definition
) {
}
