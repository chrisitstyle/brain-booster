package com.brainbooster.auth;

import com.brainbooster.config.JwtService;
import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_ShouldSaveUserAndReturnMessage_WhenEmailUnique() {
        // given
        RegisterRequest request = new RegisterRequest("johndoe", "johndoe@example.com",
                "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");

        // when
        String result = authenticationService.register(request);

        // then
        assertThat(result).isEqualTo("Account has been created");

        // verify user object construction before saving
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(savedUser.getNickname()).isEqualTo(request.getNickname());
        assertThat(savedUser.getPassword()).isEqualTo("encoded_password");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // given
        RegisterRequest request = new RegisterRequest("johndoe", "exist@example.com",
                "password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        // when
        Throwable thrown = catchThrowable(() -> authenticationService.register(request));

        // then
        assertThat(thrown)
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("User with this email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreCorrect() {
        // given
        AuthenticationRequest request = new AuthenticationRequest("johndoe@example.com",
                "password123");
        User user = TestEntities.createUser();
        String expectedToken = "jwt_token_example";

        //  when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        Authentication authResult = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authResult);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(expectedToken);

        // when
        AuthenticationResponse response = authenticationService.authenticate(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(expectedToken);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_ShouldThrowBadCredentials_WhenManagerFails() {
        // given
        AuthenticationRequest request = new AuthenticationRequest("wrong@example.com", "wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when
        Throwable thrown = catchThrowable(() -> authenticationService.authenticate(request));

        // then
        assertThat(thrown).isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_ShouldThrowUsernameNotFound_WhenUserNotFoundInRepo() {
        // given
        // edge case - AuthManager succeeds (e.g., custom provider) but user missing from db
        AuthenticationRequest request = new AuthenticationRequest("dummy@example.com",
                "password");

        //when(authenticationManager.authenticate(any())).thenReturn(null);
        Authentication dummyAuth = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        when(authenticationManager.authenticate(any())).thenReturn(dummyAuth);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> authenticationService.authenticate(request));

        // then
        assertThat(thrown).isInstanceOf(UsernameNotFoundException.class);
    }
}
