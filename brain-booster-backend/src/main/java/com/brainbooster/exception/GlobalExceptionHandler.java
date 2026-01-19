package com.brainbooster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * Global exception handler providing consistent JSON error responses ({@link ErrorDTO}) across the API.
 * <p>
 * Replaces individual @ExceptionHandler methods in controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(ResponseStatusException.class)
//    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
//        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
//    }

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

    @ExceptionHandler({AccessDeniedException.class, SelfDeletionException.class})
    public ResponseEntity<ErrorDTO> handleForbidden(RuntimeException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
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
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorDTO, status);
    }
}
