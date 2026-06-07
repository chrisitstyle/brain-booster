package com.brainbooster.flashcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Flashcard response returned by the API")
public record FlashcardDTO(

        @Schema(
                description = "Unique flashcard ID",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long flashcardId,

        @Schema(
                description = "ID of the flashcard set that contains this flashcard",
                example = "1"
        )
        Long setId,

        @Schema(
                description = "Flashcard term or question",
                example = "to go"
        )
        String term,

        @Schema(
                description = "Flashcard definition or answer",
                example = "went - gone"
        )
        String definition,
        @Schema(
                description = "Whether the flashcard is marked as starred",
                example = "false"
        )
        boolean starred
) {
}
