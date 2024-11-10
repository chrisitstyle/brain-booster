package com.brainbooster.dto;

import com.brainbooster.model.Role;

import java.time.LocalDateTime;

public record UserDTO(
        long userId,
        String nickname,
        String email,
        Role role,
        LocalDateTime createdAt) {
}
