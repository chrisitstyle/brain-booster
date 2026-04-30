package com.brainbooster.user.dto;
import jakarta.validation.constraints.NotBlank;
public record UserNicknameUpdateDTO(
        @NotBlank(message = "Nickname cannot be empty")
        String nickname) {
}

