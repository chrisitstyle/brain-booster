package com.brainbooster.flashcard.dto;

import jakarta.validation.constraints.NotBlank;

public record FlashcardUpdateDTO(
        @NotBlank(message = "Term cannot be empty")
        String term,
        @NotBlank(message = "Definition cannot be empty")
        String definition
) {
}
