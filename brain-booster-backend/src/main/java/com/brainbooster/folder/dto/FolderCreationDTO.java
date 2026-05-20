package com.brainbooster.folder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body used to create a new folder")
public record FolderCreationDTO(

        @Schema(
                description = "Folder name",
                example = "English vocabulary"
        )
        @NotBlank(message = "Folder name cannot be empty")
        String name,

        @Schema(
                description = "Folder description",
                example = "Folder with English vocabulary flashcard sets"
        )
        @NotBlank(message = "Folder description cannot be empty")
        String description
) {
}