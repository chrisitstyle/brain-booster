package com.brainbooster.auth;

import com.brainbooster.config.JwtService;
import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.security.UserPrincipal;
import com.brainbooster.user.Role;
import com.brainbooster.user.UserAccountCreator;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserAccountCreator userAccountCreator;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_ShouldCreateRegularUserAndReturnMessage() {
        // given
        RegisterRequest request = new RegisterRequest(
                "johndoe",
                "johndoe@example.com",
                "password123"
        );

        // when
        String result = authenticationService.register(request);

        // then
        assertThat(result)
                .isEqualTo("Account has been created");

        verify(userAccountCreator).create(
                request.getNickname(),
                request.getEmail(),
                request.getPassword(),
                Role.USER
        );

    }

    @Test
    void register_ShouldPropagateException_WhenEmailAlreadyExists() {
        // given
        RegisterRequest request = new RegisterRequest(
                "johndoe",
                "exist@example.com",
                "password");

        when(userAccountCreator.create(
                request.getNickname(),
                request.getEmail(),
                request.getPassword(),
                Role.USER
        )).thenThrow(
                new EmailAlreadyExistsException(
                        "User with this email already exists"
                )
        );

        // when
        Throwable thrown = catchThrowable(() -> authenticationService.register(request));

        // then
        assertThat(thrown)
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("User with this email already exists");

        verify(userAccountCreator).create(
                request.getNickname(),
                request.getEmail(),
                request.getPassword(),
                Role.USER
        );
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreCorrect() {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                "johndoe@example.com",
                "password123");

        UserPrincipal principal = TestEntities.createUserPrincipal(
                TestEntities.createUser()
        );

        String expectedToken = "jwt_token_example";

        Authentication authResult = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authResult);

        when(jwtService.generateToken(principal))
                .thenReturn(expectedToken);

        // when
        AuthenticationResponse response = authenticationService.authenticate(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(expectedToken);

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );


        verify(jwtService).generateToken(principal);
    }

    @Test
    void authenticate_ShouldThrowBadCredentials_WhenManagerFails() {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                "wrong@example.com",
                "wrongpass"
        );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(
                new BadCredentialsException("Bad credentials")
        );

        // when
        Throwable thrown = catchThrowable(() -> authenticationService.authenticate(request));

        // then
        assertThat(thrown)
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_ShouldThrow_WhenAuthenticatedPrincipalIsInvalid() {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                "dummy@example.com",
                "password");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn("invalid-principal");

        // when
        Throwable thrown = catchThrowable(
                () -> authenticationService.authenticate(request)
        );

        // then
        assertThat(thrown)
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage("Invalid authenticated principal");

        verifyNoInteractions(jwtService);
    }
}
