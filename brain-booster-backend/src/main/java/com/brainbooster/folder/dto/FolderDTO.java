package com.brainbooster.folder.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Folder response returned by the API")
public record FolderDTO(

        @Schema(
                description = "Unique folder ID",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long folderId,

        @Schema(
                description = "Nickname of the folder owner",
                example = "johndoe"
        )
        String nickname,

        @Schema(
                description = "Folder name",
                example = "English vocabulary"
        )
        String name,

        @Schema(
                description = "Folder description",
                example = "Folder with English vocabulary flashcard sets"
        )
        String description,

        @Schema(
                description = "Number of flashcard sets in the folder",
                example = "3",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long setCount,

        @Schema(
                description = "Flashcard sets assigned to the folder"
        )
        List<FlashcardSetInFolderDTO> flashcardSets
) {
}
