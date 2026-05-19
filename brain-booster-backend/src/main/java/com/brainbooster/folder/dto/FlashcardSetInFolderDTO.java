package com.brainbooster.folder.dto;

public record FlashcardSetInFolderDTO(
        Long flashcardSetId,
        String title,
        Long termCount
) {
}
