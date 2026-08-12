package com.brainbooster.user;

import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.mapper.FlashcardSetDTOMapper;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.user.dto.UserCreationDTO;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.user.dto.UserUpdateDTO;
import com.brainbooster.utils.TestEntities;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Instant FIXED_CREATED_AT =
            Instant.parse("2024-01-01T00:00:00Z");

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDTOMapper userDTOMapper;
    @Mock
    private FlashcardSetRepository flashcardSetRepository;
    @Mock
    private FlashcardSetDTOMapper flashcardSetDTOMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private UserAccountCreator userAccountCreator;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;
    private FlashcardSet flashcardSet;
    private FlashcardSetDTO flashcardSetDTO;

    @BeforeEach
    void setUp() {
        user = TestEntities.createUser();
        userDTO = TestEntities.createUserDTO();
        flashcardSet = TestEntities.createFlashcardSet();
        flashcardSetDTO = TestEntities.createFlashcardSetDTO();
    }

    @Test
    void addUser_ShouldReturnSavedUserDTO_WhenUserDoesNotExist() {
        // given
        UserCreationDTO creationDTO = TestEntities.createUserCreationDTO();

        when(userAccountCreator.create(creationDTO.nickname(), creationDTO.email(),
                creationDTO.password(),
                creationDTO.role())).thenReturn(user);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // when
        UserDTO result = userService.addUser(creationDTO);

        // then
        Assertions.assertThat(result).isEqualTo(userDTO);

        verify(userAccountCreator).create(
                creationDTO.nickname(),
                creationDTO.email(),
                creationDTO.password(),
                creationDTO.role());

        verify(userDTOMapper).apply(user);
    }

    @Test
    void addUser_ShouldPropagateEmailAlreadyExistsException() {
        // given
        UserCreationDTO creationDTO = TestEntities.createUserCreationDTO();

        when(userAccountCreator.create(
                creationDTO.nickname(),
                creationDTO.email(),
                creationDTO.password(),
                creationDTO.role()
        )).thenThrow(new EmailAlreadyExistsException(
                "User with this email already exists"));

        // when, then
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.addUser(creationDTO)
        );

        Assertions.assertThat(exception.getMessage()).isEqualTo("User with this email already exists");

        verify(userAccountCreator).create(
                creationDTO.nickname(),
                creationDTO.email(),
                creationDTO.password(),
                creationDTO.role()
        );

        verifyNoInteractions(userDTOMapper);
    }

    @Test
    void getCurrentUser_ShouldReturnUserDTO_WhenAuthenticatedUserExists() {
        // given
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.of(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // when
        UserDTO result = userService.getCurrentUser();

        // then
        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(userDTO);

        verify(userRepository).findById(authenticatedUser.userId());
        verify(userDTOMapper).apply(user);
    }

    @Test
    void getCurrentUser_ShouldThrowNoSuchElement_WhenAuthenticatedUserDoesNotExist() {
        // given
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.empty());

        // when, then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getCurrentUser()
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("Authenticated user does not exist");

        verify(userRepository).findById(authenticatedUser.userId());
        verify(userDTOMapper, never()).apply(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUserDTO_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        UserDTO userExistsDTO = userService.getUserById(1L);

        Assertions.assertThat(userExistsDTO)
                .isNotNull()
                .isEqualTo(userDTO);
    }

    @Test
    void getUserById_ShouldThrowNoSuchElement_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(1L)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with this id does not exist");
    }

    @Test
    void getAllFlashcardSetsByUserId_ShouldReturnFlashcardSetsDTO_WhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(flashcardSetRepository.findByUserId(1L)).thenReturn(List.of(flashcardSet));
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);

        List<FlashcardSetDTO> flashcardSetDTOList =
                userService.getAllFlashcardSetsByUserId(1L);

        Assertions.assertThat(flashcardSetDTOList)
                .contains(flashcardSetDTO)
                .hasSize(1);
    }

    @Test
    void getAllFlashcardSetsByUserId_ShouldThrowNoSuchElement_WhenUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getAllFlashcardSetsByUserId(1L)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with id: 1 not found");
    }

    @Test
    void getAllFlashcardSetsByUserNickname_ShouldReturnFlashcardSetsDTO_WhenUserExists() {
        String nickname = "johndoe";

        when(userRepository.existsByNickname(nickname)).thenReturn(true);
        when(flashcardSetRepository.findAllByUserNickname(nickname))
                .thenReturn(List.of(flashcardSet));
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);

        List<FlashcardSetDTO> result =
                userService.getAllFlashcardSetsByUserNickname(nickname);

        Assertions.assertThat(result)
                .contains(flashcardSetDTO)
                .hasSize(1);
    }

    @Test
    void getAllFlashcardSetsByUserNickname_ShouldThrowNoSuchElement_WhenUserDoesNotExist() {
        String nickname = "unknown";

        when(userRepository.existsByNickname(nickname)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getAllFlashcardSetsByUserNickname(nickname)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with nickname: " + nickname + " not found");
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserIsAdmin() {
        // given
        AuthenticatedUser adminUser = TestEntities.createAuthenticatedUser(2L, Role.ADMIN);

        when(currentUserProvider.getCurrentUser()).thenReturn(adminUser);

        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "newNickname",
                "new@example.com",
                "new_password",
                Role.USER
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDTOMapper.apply(any(User.class)))
                .thenReturn(new UserDTO(
                        1L,
                        "newNickname",
                        "new@example.com",
                        Role.USER,
                        FIXED_CREATED_AT
                ));

        // when
        UserDTO result = userService.updateUser(updateDTO, 1L);

        // then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.nickname()).isEqualTo("newNickname");
        Assertions.assertThat(result.email()).isEqualTo("new@example.com");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowAccessDenied_WhenUserIsNotAdmin() {
        // given
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "newNickname",
                "new@example.com",
                "new_password",
                Role.USER
        );

        // when, then
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUser(updateDTO, 2L)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("You are not allowed to update other users");
    }

    @Test
    void deleteUserById_ShouldDeleteUser_WhenAdmin() {
        // given
        AuthenticatedUser adminUser = TestEntities.createAuthenticatedUser(2L, Role.ADMIN);

        when(currentUserProvider.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.existsById(1L)).thenReturn(true);

        // when
        userService.deleteUserById(1L);

        // then
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserById_ShouldThrowNoSuchElement_WhenUserDoesNotExist() {
        // given
        AuthenticatedUser adminUser = TestEntities.createAuthenticatedUser(2L, Role.ADMIN);

        when(currentUserProvider.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.existsById(1L)).thenReturn(false);

        // when, then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUserById(1L)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with id: 1 not found");

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUserById_ShouldThrowAccessDenied_WhenUserIsNotAdmin() {
        // given
        AuthenticatedUser loggedUser = TestEntities.createAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(loggedUser);

        // when, then
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> userService.deleteUserById(3L)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("You cannot delete yourself or other users");
    }

    @Test
    void deleteUserById_ShouldThrowAccessDenied_WhenAdminTriesToDeleteSelf() {
        // given
        AuthenticatedUser adminUser = TestEntities.createAuthenticatedUser(1L, Role.ADMIN);

        when(currentUserProvider.getCurrentUser()).thenReturn(adminUser);

        // when, then
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> userService.deleteUserById(1L)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("You cannot delete yourself or other users");

        verify(userRepository, never()).deleteById(anyLong());
    }
}