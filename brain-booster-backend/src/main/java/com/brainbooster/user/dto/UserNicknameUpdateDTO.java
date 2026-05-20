package com.brainbooster.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body used to update user's nickname")
public record UserNicknameUpdateDTO(

        @Schema(
                description = "New user nickname",
                example = "john_updated"
        )
        @NotBlank(message = "Nickname cannot be empty")
        String nickname
) {
}

