package com.brainbooster.user.dto;

import com.brainbooster.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body used to update an existing user")
public record UserUpdateDTO(

        @Schema(
                description = "Updated user nickname",
                example = "john_updated"
        )
        @NotBlank(message = "Nickname cannot be empty")
        String nickname,

        @Schema(
                description = "Updated user email address",
                example = "johnupdated@example.com"
        )
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Must be a valid email format")
        String email,

        @Schema(
                description = "Updated user password",
                example = "test1234",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank(message = "Password cannot be empty")
        String password,

        @Schema(
                description = "Updated user role",
                example = "USER",
                allowableValues = {"USER", "ADMIN"}
        )
        @NotNull(message = "Role must be specified")
        Role role
) {
}
