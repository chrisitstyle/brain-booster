package com.brainbooster.flashcardset;

import com.brainbooster.user.UserDTO;

import java.time.LocalDateTime;

public record FlashcardSetDTO(
        long setId,
        UserDTO user,
        String setName,
        String description,
        LocalDateTime createdAt

) {
}
