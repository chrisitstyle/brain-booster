package com.brainbooster.user;

import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.mapper.FlashcardSetDTOMapper;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import org.assertj.core.api.Assertions;
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
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
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
        user = new User();
        user.setUserId(1L);
        user.setNickname("testUser");
        user.setEmail("test@example.com");
        user.setPassword("test_password");
        user.setRole(Role.USER);

        SecurityContextHolder.setContext(securityContext);
        userDTO = new UserDTO(1, "testUser", "test@example.com", Role.USER, null);

        flashcardSet = new FlashcardSet();
        flashcardSet.setSetId(1L);
        flashcardSet.setUser(user);
        flashcardSet.setSetName("example flashcardSet");
        flashcardSet.setDescription("example description");
        flashcardSet.setCreatedAt(LocalDateTime.parse("2024-11-13T00:28:05.738221"));

        flashcardSetDTO = new FlashcardSetDTO(flashcardSet.getSetId(),
                userDTOMapper.apply(user),
                flashcardSet.getSetName(),
                flashcardSet.getDescription(),
                flashcardSet.getCreatedAt());
    }

    @Test
    void addUser_ShouldReturnSavedUserDTO_WhenUserDoesNotExist() {


        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("secured_password");
        when(userDTOMapper.apply(user)).thenReturn(userDTO);


        UserDTO savedUserDTO = userService.addUser(user);


        Assertions.assertThat(savedUserDTO).isNotNull();
        Assertions.assertThat(savedUserDTO.userId()).isEqualTo(1);
        Assertions.assertThat(savedUserDTO.nickname()).isEqualTo("testUser");
        Assertions.assertThat(savedUserDTO.email()).isEqualTo("test@example.com");
    }


    @Test
    void addUser_ShouldThrowEmailAlreadyExists_WhenUserEmailAlreadyExists() {


        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);


        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> userService.addUser(user));

        Assertions.assertThat(exception.getMessage()).isEqualTo("User with this email already exists!");
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

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> userService.getUserById(1L));


        Assertions.assertThat(exception.getMessage()).isEqualTo("User with this id does not exist");
    }

    @Test
    void getAllUserFlashcardSetsByUserId_ShouldReturnFlashcardSetsDTO_WhenUserExists() {

        when(userRepository.existsById(1L)).thenReturn(true);
        when(flashcardSetRepository.findByUserId(1L)).thenReturn(List.of(flashcardSet));
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);

        List<FlashcardSetDTO> flashcardSetDTOList = userService.getAllFlashcardSetsByUserId(1L);

        Assertions.assertThat(flashcardSetDTOList)
                .contains(flashcardSetDTO)
                .hasSize(1);
        Assertions.assertThat(flashcardSetDTOList.getFirst())
                .isEqualTo(flashcardSetDTO);

    }

    @Test
    void getAllFlashcardSetsByUserId_ShouldThrowNoSuchElement_WhenUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> userService.getAllFlashcardSetsByUserId(1L));

        Assertions.assertThat(exception.getMessage()).isEqualTo("User with id: " + 1L + " not found");
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserIsOwner() {

       mockSecurityContext(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        User updatedUser = new User();
        updatedUser.setNickname("newNickname");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPassword("new_password");
        updatedUser.setRole(Role.USER);

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setNickname("newNickname");
        savedUser.setEmail("new@example.com");
        savedUser.setPassword("encoded_password");
        savedUser.setRole(Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDTOMapper.apply(any(User.class))).thenReturn(new UserDTO(1L, "newNickname", "new@example.com", Role.USER, null));


        UserDTO result = userService.updateUser(updatedUser, 1L);



        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.nickname()).isEqualTo("newNickname");
        Assertions.assertThat(result.email()).isEqualTo("new@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserIsAdmin() {
        User adminUser = new User();
        adminUser.setUserId(2L);
        adminUser.setRole(Role.ADMIN);

        User updatedUser = new User();
        updatedUser.setNickname("newNickname");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPassword("new_password");
        updatedUser.setRole(Role.USER);

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setNickname("newNickname");
        savedUser.setEmail("new@example.com");
        savedUser.setPassword("encoded_password");
        savedUser.setRole(Role.USER);


        mockSecurityContext(adminUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDTOMapper.apply(any(User.class)))
                .thenReturn(new UserDTO(1L,
                        "newNickname",
                        "new@example.com",
                        Role.USER,
                        null));


        UserDTO result = userService.updateUser(updatedUser, 1L);


        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.nickname()).isEqualTo("newNickname");
        Assertions.assertThat(result.email()).isEqualTo("new@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowAccessDenied_WhenUserIsNotAllowed() {

        User anotherUser = new User();
        anotherUser.setUserId(3L);
        anotherUser.setRole(Role.USER);

        User updatedUser = new User();
        updatedUser.setNickname("newNickname");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPassword("new_password");
        updatedUser.setRole(Role.USER);

        mockSecurityContext(anotherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.updateUser(updatedUser, 1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("You are not allowed to update other users");

    }

    @Test
    void updateUser_ShouldThrowNoSuchElement_WhenUserDoesNotExist() {

        User updatedUser = new User();
        updatedUser.setNickname("newNickname");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPassword("new_password");
        updatedUser.setRole(Role.USER);

        mockSecurityContext(user);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());


        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> userService.updateUser(updatedUser, 1L));
        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with id: 1 not found");

    }

    @Test
    void deleteUserById_ShouldDeleteUser_WhenAdmin() {
        User adminUser = new User();
        adminUser.setUserId(2L);
        adminUser.setRole(Role.ADMIN);


        mockSecurityContext(adminUser);
        when(userRepository.existsById(1L)).thenReturn(true);


        userService.deleteUserById(1L);


        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserById_ShouldThrowNoSuchElement_WhenUserDoesNotExist() {
        User adminUser = new User();
        adminUser.setUserId(2L);
        adminUser.setRole(Role.ADMIN);


        mockSecurityContext(adminUser);
        when(userRepository.existsById(1L)).thenReturn(false);


        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> userService.deleteUserById(1L));


        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with id: 1 not found");
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUserById_ShouldThrowAccessDenied_WhenUserIsNotAllowed() {
        User loggedUser = new User();
        loggedUser.setUserId(1L);
        loggedUser.setRole(Role.USER);


        mockSecurityContext(loggedUser);


        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.deleteUserById(3L));
        Assertions.assertThat(exception.getMessage())
                .isEqualTo("You cannot delete yourself or other users");
    }

    private void mockSecurityContext(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
    }

}
