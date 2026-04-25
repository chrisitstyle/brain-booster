package com.brainbooster.flashcardset.dto;

import com.brainbooster.user.dto.UserSummaryDTO;

import java.time.LocalDateTime;

public record FlashcardSetDTO(
        Long setId,
        UserSummaryDTO user,
        String setName,
        String description,
        LocalDateTime createdAt,
        Long termCount

) {
}
