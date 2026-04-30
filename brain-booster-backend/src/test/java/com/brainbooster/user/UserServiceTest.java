package com.brainbooster.user;

import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.mapper.FlashcardSetDTOMapper;
import com.brainbooster.user.dto.UserCreationDTO;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.user.dto.UserNicknameUpdateDTO;
import com.brainbooster.user.dto.UserUpdateDTO;
import com.brainbooster.utils.TestEntities;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(User authenticatedUser) {
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(authenticatedUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void addUser_ShouldReturnSavedUserDTO_WhenUserDoesNotExist() {
        // given
        UserCreationDTO creationDTO = TestEntities.createUserCreationDTO();

        when(userRepository.existsByEmail(creationDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("secured_password");
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // when
        UserDTO savedUserDTO = userService.addUser(creationDTO);

        // then
        Assertions.assertThat(savedUserDTO).isNotNull();
        Assertions.assertThat(savedUserDTO.userId()).isEqualTo(1L);
        Assertions.assertThat(savedUserDTO.nickname()).isEqualTo("johndoe");
        Assertions.assertThat(savedUserDTO.email()).isEqualTo("johndoe@example.com");
    }

    @Test
    void addUser_ShouldThrowEmailAlreadyExists_WhenUserEmailAlreadyExists() {
        // given
        UserCreationDTO creationDTO = TestEntities.createUserCreationDTO();
        when(userRepository.existsByEmail(creationDTO.email())).thenReturn(true);

        // when, then
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.addUser(creationDTO));

        Assertions.assertThat(exception.getMessage()).isEqualTo("User with this email already exists!");
        verify(userRepository, never()).save(any(User.class));
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

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> userService.getUserById(1L));

        Assertions.assertThat(exception.getMessage()).isEqualTo("User with this id does not exist");
    }

    @Test
    void getAllFlashcardSetsByUserId_ShouldReturnFlashcardSetsDTO_WhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(flashcardSetRepository.findByUserId(1L)).thenReturn(List.of(flashcardSet));
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);

        List<FlashcardSetDTO> flashcardSetDTOList = userService.getAllFlashcardSetsByUserId(1L);

        Assertions.assertThat(flashcardSetDTOList)
                .contains(flashcardSetDTO)
                .hasSize(1);
    }

    @Test
    void getAllFlashcardSetsByUserId_ShouldThrowNoSuchElement_WhenUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> userService.getAllFlashcardSetsByUserId(1L));

        Assertions.assertThat(exception.getMessage()).isEqualTo("User with id: 1 not found");
    }

    @Test
    void getAllFlashcardSetsByUserNickname_ShouldReturnFlashcardSetsDTO_WhenUserExists() {
        String nickname = "johndoe";
        when(userRepository.existsByNickname(nickname)).thenReturn(true);
        when(flashcardSetRepository.findAllByUserNickname(nickname)).thenReturn(List.of(flashcardSet));
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);

        List<FlashcardSetDTO> result = userService.getAllFlashcardSetsByUserNickname(nickname);

        Assertions.assertThat(result)
                .contains(flashcardSetDTO)
                .hasSize(1);
    }

    @Test
    void getAllFlashcardSetsByUserNickname_ShouldThrowNoSuchElement_WhenUserDoesNotExist() {
        String nickname = "unknown";
        when(userRepository.existsByNickname(nickname)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> userService.getAllFlashcardSetsByUserNickname(nickname));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with nickname: " + nickname + " not found");
    }

    @Test
    void updateUserNickname_ShouldReturnUpdatedUser_WhenUserIsOwner() {
        // given
        mockSecurityContext(user);
        UserNicknameUpdateDTO updateNicknameDTO = new UserNicknameUpdateDTO("newNickname");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDTOMapper.apply(any(User.class))).thenReturn(new UserDTO(1L, "newNickname", "johndoe@example.com", Role.USER, LocalDateTime.now()));

        // when
        UserDTO result = userService.updateUserNickname(updateNicknameDTO, 1L);

        // then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.nickname()).isEqualTo("newNickname");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserNickname_ShouldThrowAccessDenied_WhenUserIsNotAllowed() {
        // given
        User anotherUser = TestEntities.userBuilder().userId(3L).role(Role.USER).build();
        mockSecurityContext(anotherUser);

        UserNicknameUpdateDTO updateNicknameDTO = new UserNicknameUpdateDTO("newNickname");

        // when, then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.updateUserNickname(updateNicknameDTO, 1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("You are not allowed to update other users");
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserIsAdmin() {
        // given
        User adminUser = TestEntities.userBuilder().userId(2L).role(Role.ADMIN).build();
        mockSecurityContext(adminUser);

        UserUpdateDTO updateDTO = new UserUpdateDTO("newNickname", "new@example.com", "new_password", Role.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDTOMapper.apply(any(User.class)))
                .thenReturn(new UserDTO(1L, "newNickname", "new@example.com", Role.USER, LocalDateTime.now()));

        // when
        UserDTO result = userService.updateUser(updateDTO, 1L);

        // then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.nickname()).isEqualTo("newNickname");
        Assertions.assertThat(result.email()).isEqualTo("new@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowAccessDenied_WhenUserIsNotAdmin() {
        // given
        mockSecurityContext(user);
        UserUpdateDTO updateDTO = new UserUpdateDTO("newNickname", "new@example.com", "new_password", Role.USER);

        // when, then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.updateUser(updateDTO, 2L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("You are not allowed to update other users");
    }

    @Test
    void deleteUserById_ShouldDeleteUser_WhenAdmin() {
        User adminUser = TestEntities.userBuilder().userId(2L).role(Role.ADMIN).build();
        mockSecurityContext(adminUser);
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUserById(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserById_ShouldThrowNoSuchElement_WhenUserDoesNotExist() {
        User adminUser = TestEntities.userBuilder().userId(2L).role(Role.ADMIN).build();
        mockSecurityContext(adminUser);
        when(userRepository.existsById(1L)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> userService.deleteUserById(1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with id: 1 not found");
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUserById_ShouldThrowAccessDenied_WhenUserIsNotAdmin() {
        User loggedUser = TestEntities.createUser();
        mockSecurityContext(loggedUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.deleteUserById(3L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("You cannot delete yourself or other users");
    }

    @Test
    void deleteUserById_ShouldThrowAccessDenied_WhenAdminTriesToDeleteSelf() {
        User adminUser = TestEntities.userBuilder().userId(1L).role(Role.ADMIN).build();
        mockSecurityContext(adminUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.deleteUserById(1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("You cannot delete yourself or other users");
        verify(userRepository, never()).deleteById(anyLong());
    }
}