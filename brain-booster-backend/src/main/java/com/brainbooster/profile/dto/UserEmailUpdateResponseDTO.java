package com.brainbooster.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after email update")
public record UserEmailUpdateResponseDTO(

        @Schema(example = "john.updated@example.com")
        String email,

        @Schema(description = "New JWT containing the updated email as its subject")
        String token

) {
}
