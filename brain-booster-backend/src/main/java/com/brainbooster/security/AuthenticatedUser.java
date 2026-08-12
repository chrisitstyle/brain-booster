package com.brainbooster.security;

import com.brainbooster.user.Role;

public record AuthenticatedUser(
        Long userId,
        Role role
) {
}
