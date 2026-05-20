package com.brainbooster.folder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body used to update an existing folder")
public record FolderUpdateDTO(

        @Schema(
                description = "Updated folder name",
                example = "Advanced English vocabulary"
        )
        @NotBlank(message = "Name cannot be empty")
        String name,

        @Schema(
                description = "Updated folder description",
                example = "Updated folder with advanced English vocabulary flashcard sets"
        )
        @NotBlank(message = "Description cannot be empty")
        String description
) {
}
