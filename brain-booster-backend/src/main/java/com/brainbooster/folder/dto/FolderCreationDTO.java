package com.brainbooster.folder.dto;

import jakarta.validation.constraints.NotBlank;

public record FolderCreationDTO(
        @NotBlank(message = "Folder name cannot be empty")
        String name,
        @NotBlank(message = "Folder description cannot be empty")
        String description
) {
}