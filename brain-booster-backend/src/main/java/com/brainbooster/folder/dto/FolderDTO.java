package com.brainbooster.folder.dto;

import java.util.List;

public record FolderDTO(
        Long folderId,
        String nickname,
        String name,
        String description,
        Long setCount,
        List<FlashcardSetInFolderDTO> flashcardSets
) {
}
