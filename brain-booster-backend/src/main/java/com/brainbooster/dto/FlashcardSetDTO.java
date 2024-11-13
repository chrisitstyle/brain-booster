package com.brainbooster.dto;

import java.time.LocalDateTime;

public record FlashcardSetDTO(
        long setId,
        UserDTO user,
        String setName,
        String description,
        LocalDateTime createdAt

) {
}
