package com.brainbooster.flashcardset.dto;

import jakarta.validation.constraints.NotBlank;

public record FlashcardSetUpdateDTO(
        @NotBlank(message = "Set name cannot be empty")
        String setName,
        @NotBlank(message = "Description cannot be empty")
        String description

) {
}

