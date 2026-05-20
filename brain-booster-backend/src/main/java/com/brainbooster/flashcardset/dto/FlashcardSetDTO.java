package com.brainbooster.flashcardset.dto;

import com.brainbooster.user.dto.UserSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Flashcard set response returned by the API")
public record FlashcardSetDTO(

        @Schema(
                description = "Unique flashcard set ID",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long setId,

        @Schema(description = "Short information about the flashcard set owner")
        UserSummaryDTO user,

        @Schema(
                description = "Flashcard set name",
                example = "English irregular verbs"
        )
        String setName,

        @Schema(
                description = "Flashcard set description",
                example = "A set for learning the most common English irregular verbs"
        )
        String description,

        @Schema(
                description = "Date and time when the flashcard set was created",
                example = "2026-05-19T21:30:00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Number of flashcards in the set",
                example = "25",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long termCount
) {
}
