package com.brainbooster.flashcardset.dto;

import com.brainbooster.user.UserDTO;

import java.time.LocalDateTime;

public record FlashcardSetDTO(
        Long setId,
        UserDTO user,
        String setName,
        String description,
        LocalDateTime createdAt,
        Long termCount

) {
}
