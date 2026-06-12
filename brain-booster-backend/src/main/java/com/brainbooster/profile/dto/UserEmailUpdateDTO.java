package com.brainbooster.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body used to update user's email")
public record UserEmailUpdateDTO(

        @Schema(
                description = "New user email",
                example = "johnemailupdated@example.com"
        )
        @NotBlank(message = "New email cannot be empty")
        @Email(message = "New email must be valid")
        String newEmail
) {
}

