package com.brainbooster.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO representing a standardized error response.
 * <p>
 * This record is used by the GlobalExceptionHandler to return consistent
 * JSON error structures to the client whenever an exception occurs in the API.
 *
 * @param message   the user-friendly error message describing what went wrong
 * @param status    the string representation of the HTTP status (e.g., "NOT_FOUND")
 * @param timestamp the exact date and time when the error occurred
 */
public record ErrorDTO(
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        String status,
        LocalDateTime timestamp
) {
}
