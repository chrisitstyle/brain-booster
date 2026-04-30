package com.brainbooster.user.dto;

import java.time.LocalDateTime;

public record UserSummaryDTO(String nickname, LocalDateTime createdAt) {
}
