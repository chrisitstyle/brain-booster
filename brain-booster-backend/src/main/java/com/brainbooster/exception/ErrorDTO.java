package com.brainbooster.exception;

import java.time.Instant;

/**
 * DTO representing a standardized error response.
 * <p>
 * This record is used by the GlobalExceptionHandler to return consistent
 * JSON error structures to the client whenever an exception occurs in the API.
 *
 * @param message   the user-friendly error message describing what went wrong
 * @param status    the string representation of the HTTP status, for example "NOT_FOUND"
 * @param timestamp the exact moment when the error occurred
 */
public record ErrorDTO(
        String message,
        String status,
        Instant timestamp
) {
}