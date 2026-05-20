package com.brainbooster.folder.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Short flashcard set summary returned inside folder details")
public record FlashcardSetInFolderDTO(

        @Schema(
                description = "Unique flashcard set ID",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long flashcardSetId,

        @Schema(
                description = "Flashcard set title",
                example = "English vocabulary"
        )
        String title,

        @Schema(
                description = "Number of flashcards in the set",
                example = "25",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long termCount
) {
}
