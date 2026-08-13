package com.brainbooster.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private static final String ERROR_MESSAGE = "Test error";

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleValidationException_ShouldReturnBadRequestWithValidationMessage() {
        // given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
                new Object(), "request");

        bindingResult.addError(
                new FieldError(
                        "request",
                        "email",
                        "must not be blank"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                        mock(MethodParameter.class),
                        bindingResult);

        // when
        ResponseEntity<ErrorDTO> response = globalExceptionHandler.handleValidationException(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "email: must not be blank");
    }

    @Test
    void handleValidationException_ShouldReturnFallbackMessage_WhenNoFieldErrorsExist() {
        // given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
                new Object(), "request");

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                        mock(MethodParameter.class),
                        bindingResult);

        // when
        ResponseEntity<ErrorDTO> response =
                globalExceptionHandler.handleValidationException(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Validation failed");
    }

    @Test
    void handleInvalidJson_ShouldReturnBadRequest() {
        // given
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        // when
        ResponseEntity<ErrorDTO> response =
                globalExceptionHandler.handleInvalidJson(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Invalid or malformed JSON request");
    }

    @Test
    void handleBadCredentials_ShouldReturnUnauthorized() {
        // given
        BadCredentialsException exception = new BadCredentialsException(ERROR_MESSAGE);

        // when
        ResponseEntity<ErrorDTO> response =
                globalExceptionHandler.handleBadCredentials(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password");
    }

    @Test
    void handleNotFound_ShouldReturnNotFound() {
        // given
        ResourceNotFoundException exception = new ResourceNotFoundException(ERROR_MESSAGE);

        // when
        ResponseEntity<ErrorDTO> response = globalExceptionHandler.handleNotFound(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.NOT_FOUND,
                ERROR_MESSAGE);
    }

    @Test
    void handleInvalidGameMode_ShouldReturnBadRequest() {
        // given
        InvalidGameModeException exception = new InvalidGameModeException("invalid-mode");

        // when
        ResponseEntity<ErrorDTO> response = globalExceptionHandler.handleInvalidGameMode(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Invalid game mode: invalid-mode");
    }

    @Test
    void handleBadRequest_ShouldReturnBadRequest() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException(ERROR_MESSAGE);

        // when
        ResponseEntity<ErrorDTO> response = globalExceptionHandler.handleBadRequest(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                ERROR_MESSAGE);
    }

    @Test
    void handleEmailAlreadyExists_ShouldReturnUnprocessableContent() {
        // given
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(ERROR_MESSAGE);

        // when
        ResponseEntity<ErrorDTO> response = globalExceptionHandler.handleEmailAlreadyExists(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.UNPROCESSABLE_CONTENT,
                ERROR_MESSAGE);
    }

    @Test
    void handleNicknameAlreadyExists_ShouldReturnConflict() {
        // given
        NicknameAlreadyExistsException exception = new NicknameAlreadyExistsException(ERROR_MESSAGE);

        // when
        ResponseEntity<ErrorDTO> response =
                globalExceptionHandler.handleNicknameAlreadyExists(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.CONFLICT,
                ERROR_MESSAGE);
    }

    @Test
    void handleForbidden_ShouldReturnForbidden_ForAccessDeniedException() {
        // given
        AccessDeniedException exception = new AccessDeniedException(ERROR_MESSAGE);

        // when
        ResponseEntity<ErrorDTO> response =
                globalExceptionHandler.handleForbidden(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                ERROR_MESSAGE);
    }

    @Test
    void handleForbidden_ShouldReturnForbidden_ForSelfDeletionException() {
        // given
        SelfDeletionException exception = new SelfDeletionException(ERROR_MESSAGE);

        // when
        ResponseEntity<ErrorDTO> response =
                globalExceptionHandler.handleForbidden(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                ERROR_MESSAGE);
    }

    @Test
    void handleUnexpectedException_ShouldReturnInternalServerError() {
        // given
        Exception exception = new Exception(ERROR_MESSAGE);

        // when
        ResponseEntity<ErrorDTO> response = globalExceptionHandler.handleUnexpectedException(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred"
        );
    }

    private void assertErrorResponse(
            ResponseEntity<ErrorDTO> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertThat(response.getStatusCode())
                .isEqualTo(expectedStatus);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().message())
                .isEqualTo(expectedMessage);

        assertThat(response.getBody().status())
                .isEqualTo(expectedStatus.name());

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }
}
