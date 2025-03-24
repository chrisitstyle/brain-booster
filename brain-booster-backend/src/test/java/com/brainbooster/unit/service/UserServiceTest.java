package com.brainbooster.unit.service;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetDTO;
import com.brainbooster.flashcardset.FlashcardSetDTOMapper;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.user.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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


        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
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
    void addUser_ShouldThrowUnprocessableEntity_WhenUserEmailAlreadyExists() {


        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.addUser(user));


        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        Assertions.assertThat(exception.getReason()).isEqualTo("User with this email already exists");
    }

    @Test
    void getUserById_ShouldReturnUserDTO_WhenUserExists() {

        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        UserDTO userExistsDTO = userService.getUserById(1L);

        Assertions.assertThat(userExistsDTO)
                .isNotNull()
                .isEqualTo(userDTO);
    }

    @Test
    void getUserById_ShouldThrowNotFound_WhenUserDoesNotExist() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.getUserById(1L));

        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(exception.getReason()).isEqualTo("User with this id does not exist");
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
    void getAllFlashcardSetsByUserId_ShouldThrowNotFound_WhenUserDoesNotExist() {
        // arrange
        when(userRepository.existsById(1L)).thenReturn(false);
        // act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.getAllFlashcardSetsByUserId(1L));
        // assert
        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(exception.getReason()).isEqualTo("User with id: " + 1L + " not found");
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserIsOwner() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
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


        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDTOMapper.apply(any(User.class))).thenReturn(new UserDTO(1L, "newNickname", "new@example.com", Role.USER, null));


        UserDTO result = userService.updateUser(updatedUser, 1L);


        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.nickname()).isEqualTo("newNickname");
        Assertions.assertThat(result.email()).isEqualTo("new@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowForbidden_WhenUserIsNotAllowed() {

        User anotherUser = new User();
        anotherUser.setUserId(3L);
        anotherUser.setRole(Role.USER);

        User updatedUser = new User();
        updatedUser.setNickname("newNickname");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPassword("new_password");
        updatedUser.setRole(Role.USER);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(anotherUser);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser(updatedUser, 1L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

    }

    @Test
    void updateUser_ShouldThrowNotFound_WhenUserDoesNotExist() {

        User updatedUser = new User();
        updatedUser.setNickname("newNickname");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPassword("new_password");
        updatedUser.setRole(Role.USER);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser(updatedUser, 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

    }

    @Test
    void deleteUserById_ShouldDeleteUser_WhenAdmin() {
        User adminUser = new User();
        adminUser.setUserId(2L);
        adminUser.setRole(Role.ADMIN);


        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(userRepository.existsById(1L)).thenReturn(true);


        userService.deleteUserById(1L);


        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserById_ShouldThrowNotFound_WhenUserDoesNotExist() {
        User adminUser = new User();
        adminUser.setUserId(2L);
        adminUser.setRole(Role.ADMIN);


        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(userRepository.existsById(1L)).thenReturn(false);


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.deleteUserById(1L));


        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUserById_ShouldThrowForbidden_WhenUserIsNotAllowed() {
        User loggedUser = new User();
        loggedUser.setUserId(1L);
        loggedUser.setRole(Role.USER);


        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(loggedUser);


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.deleteUserById(3L));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }


}
