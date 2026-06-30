package com.brainbooster.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Global exception handler providing consistent JSON error responses ({@link ErrorDTO}) across the API.
 * <p>
 * Replaces individual @ExceptionHandler methods in controllers.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = "Validation failed";
        }

        return createErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> handleInvalidJson(HttpMessageNotReadableException ex) {
        return createErrorResponse("Invalid or malformed JSON request", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorDTO> handleNotFound(NoSuchElementException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleBadRequest(IllegalArgumentException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(NicknameAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleNicknameAlreadyExists(NicknameAlreadyExistsException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({AccessDeniedException.class, SelfDeletionException.class})
    public ResponseEntity<ErrorDTO> handleForbidden(RuntimeException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleUnexpectedException(Exception ex) {
        log.error("Unexpected server error", ex);

        return createErrorResponse(
                "An unexpected server error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Helper method to construct a standard {@link ResponseEntity} containing an {@link ErrorDTO}.
     *
     * @param message the error message to display to the client
     * @param status  the HTTP status to return (determines both the code and the message in the DTO)
     * @return the complete response entity ready to be returned by the handler
     */
    private ResponseEntity<ErrorDTO> createErrorResponse(String message, HttpStatus status) {
        ErrorDTO errorDTO = new ErrorDTO(
                message,
                status.name(),
                Instant.now()
        );
        return new ResponseEntity<>(errorDTO, status);
    }
}
