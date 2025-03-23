package com.brainbooster.user;

import java.time.LocalDateTime;

public record UserDTO(
        long userId,
        String nickname,
        String email,
        Role role,
        LocalDateTime createdAt) {
}
