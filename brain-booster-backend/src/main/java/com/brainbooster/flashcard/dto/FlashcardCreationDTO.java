package com.brainbooster.flashcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FlashcardCreationDTO(
        @NotNull(message = "setId cannot be empty")
        Long setId,
        @NotBlank(message = "term cannot be empty")
        String term,
        @NotBlank(message = "definition cannot be empty")
        String definition
) {
}
