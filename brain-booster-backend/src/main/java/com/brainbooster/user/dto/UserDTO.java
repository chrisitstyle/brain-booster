package com.brainbooster.user.dto;

import com.brainbooster.user.Role;

import java.time.LocalDateTime;

public record UserDTO(
        Long userId,
        String nickname,
        String email,
        Role role,
        LocalDateTime createdAt) {
}
