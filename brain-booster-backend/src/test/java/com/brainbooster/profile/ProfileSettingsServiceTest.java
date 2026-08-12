package com.brainbooster.profile;

import com.brainbooster.config.JwtService;
import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.exception.NicknameAlreadyExistsException;
import com.brainbooster.profile.dto.UserEmailUpdateDTO;
import com.brainbooster.profile.dto.UserEmailUpdateResponseDTO;
import com.brainbooster.profile.dto.UserNicknameUpdateDTO;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.security.UserPrincipal;
import com.brainbooster.user.User;
import com.brainbooster.user.UserDTOMapper;
import com.brainbooster.user.UserRepository;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileSettingsServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDTOMapper userDTOMapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ProfileSettingsService profileSettingsService;

    private User user;
    private UserDTO userDTO;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        user = TestEntities.createUser();
        userDTO = TestEntities.createUserDTO();
        authenticatedUser = TestEntities.createAuthenticatedUser();

        when(currentUserProvider.getCurrentUser()).thenReturn(authenticatedUser);
    }

    @Test
    void updateNickname_ShouldUpdateNickname_WhenNicknameIsAvailable() {
        // given
        UserNicknameUpdateDTO request = new UserNicknameUpdateDTO("  newNickname  ");

        UserDTO updatedUserDTO = new UserDTO(
                user.getUserId(),
                "newNickname",
                user.getEmail(),
                user.getRole(),
                Instant.parse("2026-01-19T23:00:00Z")
        );

        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByNickname("newNickname"))
                .thenReturn(false);

        when(userDTOMapper.apply(user))
                .thenReturn(updatedUserDTO);

        // when
        UserDTO result = profileSettingsService.updateNickname(request);

        // then
        assertThat(result).isEqualTo(updatedUserDTO);
        assertThat(result.nickname()).isEqualTo("newNickname");
        assertThat(user.getNickname()).isEqualTo("newNickname");

        verify(userRepository).findById(user.getUserId());
        verify(userRepository).existsByNickname("newNickname");
        verify(userDTOMapper).apply(user);
    }

    @Test
    void updateNickname_ShouldReturnCurrentUser_WhenNicknameHasNotChanged() {
        // given
        UserNicknameUpdateDTO request = new UserNicknameUpdateDTO("  johndoe  ");

        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.of(user));

        when(userDTOMapper.apply(user))
                .thenReturn(userDTO);

        // when
        UserDTO result = profileSettingsService.updateNickname(request);

        // then
        assertThat(result).isEqualTo(userDTO);
        assertThat(user.getNickname()).isEqualTo("johndoe");

        verify(userRepository, never())
                .existsByNickname(anyString());

        verify(userDTOMapper).apply(user);
    }

    @Test
    void updateNickname_ShouldThrowNicknameAlreadyExists_WhenNicknameIsTaken() {
        // given
        UserNicknameUpdateDTO request =
                new UserNicknameUpdateDTO("takenNickname");

        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByNickname("takenNickname"))
                .thenReturn(true);

        // when, then
        assertThatThrownBy(
                () -> profileSettingsService.updateNickname(request)
        )
                .isInstanceOf(NicknameAlreadyExistsException.class)
                .hasMessage("Nickname is already taken");

        assertThat(user.getNickname()).isEqualTo("johndoe");

        verify(userRepository).existsByNickname("takenNickname");
        verifyNoInteractions(userDTOMapper);
    }

    @Test
    void updateEmail_ShouldUpdateEmailAndGenerateToken_WhenEmailIsAvailable() {
        // given
        UserEmailUpdateDTO request = new UserEmailUpdateDTO("  NEW.EMAIL@Example.COM  ");

        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("new.email@example.com"))
                .thenReturn(false);

        when(jwtService.generateToken(any(UserPrincipal.class)))
                .thenReturn("new-jwt-token");

        // when
        UserEmailUpdateResponseDTO result = profileSettingsService.updateEmail(request);

        // then
        assertThat(result.email()).isEqualTo("new.email@example.com");
        assertThat(result.token()).isEqualTo("new-jwt-token");
        assertThat(user.getEmail()).isEqualTo("new.email@example.com");

        verify(userRepository).existsByEmail("new.email@example.com");
        verify(jwtService).generateToken(
                argThat(principal ->
                        principal.userId().equals(user.getUserId())
                                && principal.email().equals("new.email@example.com")
                                && principal.role().equals(user.getRole())));
    }

    @Test
    void updateEmail_ShouldGenerateTokenWithoutUpdatingEmail_WhenEmailHasNotChanged() {
        // given
        UserEmailUpdateDTO request = new UserEmailUpdateDTO("  JOHNDOE@EXAMPLE.COM  ");
        UserPrincipal expectedPrincipal = UserPrincipal.from(user);
        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(expectedPrincipal))
                .thenReturn("refreshed-jwt-token");

        // when
        UserEmailUpdateResponseDTO result = profileSettingsService.updateEmail(request);

        // then
        assertThat(result.email()).isEqualTo("johndoe@example.com");
        assertThat(result.token()).isEqualTo("refreshed-jwt-token");
        assertThat(user.getEmail()).isEqualTo("johndoe@example.com");

        verify(userRepository, never())
                .existsByEmail(anyString());

        verify(jwtService).generateToken(expectedPrincipal);
    }

    @Test
    void updateEmail_ShouldThrowEmailAlreadyExists_WhenEmailIsTaken() {
        // given
        UserEmailUpdateDTO request = new UserEmailUpdateDTO("taken@example.com");

        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("taken@example.com"))
                .thenReturn(true);

        // when, then
        assertThatThrownBy(
                () -> profileSettingsService.updateEmail(request)
        )
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email is already taken");

        assertThat(user.getEmail()).isEqualTo("johndoe@example.com");

        verify(userRepository).existsByEmail("taken@example.com");
        verifyNoInteractions(jwtService);
    }

    @Test
    void updateNickname_ShouldThrowNoSuchElementEx_WhenAuthenticatedUserDoesNotExist() {
        // given
        UserNicknameUpdateDTO request =
                new UserNicknameUpdateDTO("newNickname");

        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(
                () -> profileSettingsService.updateNickname(request)
        )
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Authenticated user not found");

        verify(userRepository).findById(authenticatedUser.userId());
        verify(userRepository, never())
                .existsByNickname(anyString());

        verifyNoInteractions(userDTOMapper, jwtService);
    }
}

