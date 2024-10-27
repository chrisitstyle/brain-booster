package com.brainbooster.unit.service;

import com.brainbooster.model.Role;
import com.brainbooster.model.User;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1);
        user.setNickname("testUser");
        user.setEmail("test@example.com");
        user.setPassword("test_password");
        user.setRole(Role.USER);
    }

    @Test
    void UserService_AddUser_ReturnsSavedUser() {

        // arrange
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        // act
        User savedUser = userService.addUser(user);

        // assert
        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getUserId()).isEqualTo(1);
        Assertions.assertThat(savedUser.getNickname()).isEqualTo("testUser");
        Assertions.assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
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
    void UserService_GetUserById_ShouldReturnUser_WhenUserExists(){
        //arrange
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        //act
        User userExists = userService.getUserById(1L);
        //assert
        Assertions.assertThat(userExists)
                .isNotNull()
                .isEqualTo(user);
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
    void UserService_UpdateUser_ReturnsUpdatedUser() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(user));
        when(userRepository.save(user)).thenReturn(user);

        User updatedUserReturn = userService.updateUser(user,userId);
        Assertions.assertThat(updatedUserReturn).isNotNull();
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
