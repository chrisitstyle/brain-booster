package com.brainbooster.profile;

import com.brainbooster.config.JwtService;
import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.exception.NicknameAlreadyExistsException;
import com.brainbooster.profile.dto.UserEmailUpdateDTO;
import com.brainbooster.profile.dto.UserEmailUpdateResponseDTO;
import com.brainbooster.profile.dto.UserNicknameUpdateDTO;
import com.brainbooster.user.User;
import com.brainbooster.user.UserDTOMapper;
import com.brainbooster.user.UserRepository;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.utils.TestEntities;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
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

    @InjectMocks
    private ProfileSettingsService profileSettingsService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = TestEntities.createUser();
        userDTO = TestEntities.createUserDTO();

        mockSecurityContext(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateNickname_ShouldUpdateNickname_WhenNicknameIsAvailable() {
        // given
        UserNicknameUpdateDTO request =
                new UserNicknameUpdateDTO("  newNickname  ");

        UserDTO updatedUserDTO = new UserDTO(
                user.getUserId(),
                "newNickname",
                user.getEmail(),
                user.getRole(),
                LocalDateTime.of(2026, 1, 19, 23, 0)
        );

        when(userRepository.findById(user.getUserId()))
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
        UserNicknameUpdateDTO request =
                new UserNicknameUpdateDTO("  johndoe  ");

        when(userRepository.findById(user.getUserId()))
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

        when(userRepository.findById(user.getUserId()))
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
        UserEmailUpdateDTO request =
                new UserEmailUpdateDTO("  NEW.EMAIL@Example.COM  ");

        when(userRepository.findById(user.getUserId()))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("new.email@example.com"))
                .thenReturn(false);

        when(jwtService.generateToken(user))
                .thenReturn("new-jwt-token");

        // when
        UserEmailUpdateResponseDTO result =
                profileSettingsService.updateEmail(request);

        // then
        assertThat(result.email()).isEqualTo("new.email@example.com");
        assertThat(result.token()).isEqualTo("new-jwt-token");
        assertThat(user.getEmail()).isEqualTo("new.email@example.com");

        verify(userRepository).existsByEmail("new.email@example.com");
        verify(jwtService).generateToken(user);
    }

    @Test
    void updateEmail_ShouldGenerateTokenWithoutUpdatingEmail_WhenEmailHasNotChanged() {
        // given
        UserEmailUpdateDTO request =
                new UserEmailUpdateDTO("  JOHNDOE@EXAMPLE.COM  ");

        when(userRepository.findById(user.getUserId()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("refreshed-jwt-token");

        // when
        UserEmailUpdateResponseDTO result =
                profileSettingsService.updateEmail(request);

        // then
        assertThat(result.email()).isEqualTo("johndoe@example.com");
        assertThat(result.token()).isEqualTo("refreshed-jwt-token");
        assertThat(user.getEmail()).isEqualTo("johndoe@example.com");

        verify(userRepository, never())
                .existsByEmail(anyString());

        verify(jwtService).generateToken(user);
    }

    @Test
    void updateEmail_ShouldThrowEmailAlreadyExists_WhenEmailIsTaken() {
        // given
        UserEmailUpdateDTO request =
                new UserEmailUpdateDTO("taken@example.com");

        when(userRepository.findById(user.getUserId()))
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

        when(userRepository.findById(user.getUserId()))
                .thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(
                () -> profileSettingsService.updateNickname(request)
        )
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Authenticated user not found");

        verify(userRepository).findById(user.getUserId());
        verify(userRepository, never())
                .existsByNickname(anyString());

        verifyNoInteractions(userDTOMapper, jwtService);
    }

    private void mockSecurityContext(User authenticatedUser) {
        Authentication authentication = mock(Authentication.class);

        lenient()
                .when(authentication.isAuthenticated())
                .thenReturn(true);

        lenient()
                .when(authentication.getPrincipal())
                .thenReturn(authenticatedUser);

        SecurityContext securityContext = mock(SecurityContext.class);

        lenient()
                .when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
}

