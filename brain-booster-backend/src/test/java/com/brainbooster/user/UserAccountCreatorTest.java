package com.brainbooster.user;

import com.brainbooster.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountCreatorTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserAccountCreator userAccountCreator;

    @BeforeEach
    void setUp() {
        userAccountCreator = new UserAccountCreator(
                userRepository,
                passwordEncoder);
    }

    @Test
    void create_ShouldCreateAndSaveUser_WhenEmailIsAvailable() {
        // given
        String nickname = "johndoe";
        String email = "johndoe@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encoded_password";
        Role role = Role.USER;

        when(userRepository.existsByEmail(email)).thenReturn(false);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userAccountCreator.create(
                nickname,
                email,
                rawPassword,
                role);

        // then
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        assertThat(result.getRole()).isEqualTo(role);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_ShouldSaveUserWithRequestedRole() {
        // given
        String nickname = "admin";
        String email = "admin@example.com";
        String rawPassword = "password123";

        when(userRepository.existsByEmail(email)).thenReturn(false);

        when(passwordEncoder.encode(rawPassword)).thenReturn("encoded_password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userAccountCreator.create(
                nickname,
                email,
                rawPassword,
                Role.ADMIN);

        // then
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void create_ShouldEncodePasswordBeforeSavingUser() {
        // given
        String rawPassword = "password123";
        String encodedPassword = "encoded_password";

        when(userRepository.existsByEmail("johndoe@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(rawPassword))
                .thenReturn(encodedPassword);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userAccountCreator.create(
                "johndoe",
                "johndoe@example.com",
                rawPassword,
                Role.USER);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getPassword()).isEqualTo(encodedPassword);

        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void create_ShouldSetCreatedAt() {
        // given
        when(userRepository.existsByEmail("johndoe@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded_password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userAccountCreator.create(
                "johndoe",
                "johndoe@example.com",
                "password123",
                Role.USER);

        // then
        assertThat(result.getCreatedAt())
                .isNotNull();
    }

    @Test
    void create_ShouldThrowEmailAlreadyExists_WhenEmailIsNotAvailable() {
        // given
        String email = "existing@example.com";

        when(userRepository.existsByEmail(email))
                .thenReturn(true);

        // when, then
        assertThatThrownBy(() -> userAccountCreator.create(
                        "johndoe",
                        email,
                        "password123",
                        Role.USER
                )
        )
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage(
                        "User with this email already exists");

        verify(userRepository).existsByEmail(email);

        verifyNoInteractions(passwordEncoder);

        verify(userRepository, never())
                .save(any(User.class));
    }
}
