package com.brainbooster.exception;

/**
 * Indicates that a review cannot be applied to the current state.
 *
 * <p>Examples include reusing a request ID with different data
 * or rating a previously reviewed card before its due date.
 */
public class ReviewConflictException extends RuntimeException {

    public ReviewConflictException(String message) {
        super(message);
    }
}
