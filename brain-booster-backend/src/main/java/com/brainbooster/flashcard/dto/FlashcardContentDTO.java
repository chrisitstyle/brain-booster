package com.brainbooster.flashcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Flashcard content used when creating a flashcard set")
public record FlashcardContentDTO(

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
