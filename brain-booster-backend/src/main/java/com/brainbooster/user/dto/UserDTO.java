package com.brainbooster.user.dto;

import com.brainbooster.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "User response returned by the API")
public record UserDTO(

        @Schema(
                description = "Unique user ID",
                example = "100",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long userId,

        @Schema(
                description = "User nickname",
                example = "billy"
        )
        String nickname,

        @Schema(
                description = "User email address",
                example = "billy@example.com"
        )
        String email,

        @Schema(
                description = "User role",
                example = "USER",
                allowableValues = {"USER", "ADMIN"}
        )
        Role role,

        @Schema(
                description = "Date and time when the user account was created",
                example = "2026-05-19T21:30:00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant createdAt
) {
}
