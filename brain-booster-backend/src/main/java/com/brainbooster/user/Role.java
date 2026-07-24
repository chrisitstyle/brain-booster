package com.brainbooster.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User role defining access level and system permissions")
public enum Role {
    USER, ADMIN
}