package com.brainbooster.user.dto;

import com.brainbooster.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreationDTO(
        @NotBlank(message = "Nickname cannot be empty")
        String nickname,
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Must be a valid email format")
        String email,
        @NotBlank(message = "Password cannot be empty")
        String password,
        @NotNull(message = "Role must be specified")
        Role role
) {
}
