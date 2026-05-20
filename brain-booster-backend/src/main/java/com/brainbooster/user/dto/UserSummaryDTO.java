package com.brainbooster.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Short user summary returned in public resources")
public record UserSummaryDTO(

        @Schema(
                description = "User nickname",
                example = "johndoe"
        )
        String nickname,

        @Schema(
                description = "Date and time when the user account was created",
                example = "2026-05-19T21:30:00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDateTime createdAt
) {
}
