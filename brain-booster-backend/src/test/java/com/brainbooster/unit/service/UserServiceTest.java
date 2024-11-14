package com.brainbooster.unit.service;

import com.brainbooster.dto.FlashcardSetDTO;
import com.brainbooster.dto.UserDTO;
import com.brainbooster.dtomapper.FlashcardSetDTOMapper;
import com.brainbooster.dtomapper.UserDTOMapper;
import com.brainbooster.model.FlashcardSet;
import com.brainbooster.model.Role;
import com.brainbooster.model.User;
import com.brainbooster.repository.FlashcardSetRepository;
import com.brainbooster.repository.UserRepository;
import com.brainbooster.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        user = new User();
        user.setUserId(1);
        user.setNickname("testUser");
        user.setEmail("test@example.com");
        user.setPassword("test_password");
        user.setRole(Role.USER);

        userDTO = new UserDTO(1, "testUser", "test@example.com",Role.USER, null);

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
    void UserService_AddUser_ReturnsSavedUserDTO() {

        // arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("secured_password");

        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // act
        UserDTO savedUserDTO = userService.addUser(user);

        // assert
        Assertions.assertThat(savedUserDTO).isNotNull();
        Assertions.assertThat(savedUserDTO.userId()).isEqualTo(1);
        Assertions.assertThat(savedUserDTO.nickname()).isEqualTo("testUser");
        Assertions.assertThat(savedUserDTO.email()).isEqualTo("test@example.com");
    }


    @Test
    void UserService_AddUser_ThrowsResponseStatusException_WhenUserEmailAlreadyExist(){

        //arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        //act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.addUser(user));

        //assert
        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        Assertions.assertThat(exception.getReason()).isEqualTo("User with this email already exists");
    }

    @Test
    void UserService_GetUserById_ShouldReturnUserDTO_WhenUserExists(){
        //arrange
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        //act
        UserDTO userExistsDTO = userService.getUserById(1L);
        //assert
        Assertions.assertThat(userExistsDTO)
                .isNotNull()
                .isEqualTo(userDTO);
    }

    @Test
    void userService_GetUserById_ShouldThrowResponseStatusException_WhenUserDoesNotExist(){
        //arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        //act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.getUserById(1L));
        //assert
        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(exception.getReason()).isEqualTo("User with this id does not exist");
    }

    @Test
    void UserService_GetAllUserFlashcardSetsByUserId_ShouldReturnFlashcardSetsDTO_WhenUserExists(){
        // arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        when(flashcardSetRepository.findByUserId(1L)).thenReturn(List.of(flashcardSet));
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);
        // act
        List<FlashcardSetDTO> flashcardSetDTOList = userService.getAllFlashcardSetsByUserId(1L);

        // assert
        Assertions.assertThat(flashcardSetDTOList)
                .contains(flashcardSetDTO)
                        .hasSize(1);
        Assertions.assertThat(flashcardSetDTOList.getFirst())
                .isEqualTo(flashcardSetDTO);

    }
    @Test
    void UserService_GetAllUserFlashcardSetsByUserId_ThrowsResponseStatusException_WhenUserDoesNotExist(){
        // arrange
        when(userRepository.existsById(1L)).thenReturn(false);
        // act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.getAllFlashcardSetsByUserId(1L));
        // assert
        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(exception.getReason()).isEqualTo("User with id: " + 1L + " not found");
    }

    @Test
    void UserService_UpdateUser_ReturnsUpdatedUserDTO() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        UserDTO updatedUserDTO = userService.updateUser(user,userId);
        Assertions.assertThat(updatedUserDTO)
                .isNotNull()
                .isEqualTo(userDTO);
    }

    @Test
    void UserService_UpdateUser_ThrowsResponseStatusException_WhenUserDoesNotExist(){
        long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser(user,userId));

        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void userService_DeleteUserById_ShouldDeleteUser_WhenUserExists(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserById(1L);

        assertAll(() -> userService.deleteUserById(1L));
    }

    @Test
    void userService_DeleteUserById_ThrowsResponseStatusException_WhenUserDoesNotExist(){

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.deleteUserById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User with id: " + 1L + " doesn't exist", exception.getReason());
        verify(userRepository, never()).deleteById(anyLong());
    }




}
