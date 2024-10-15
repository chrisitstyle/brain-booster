package com.brainbooster.unit.controller;

import com.brainbooster.controller.UserController;
import com.brainbooster.model.Role;
import com.brainbooster.model.User;
import com.brainbooster.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUserId(1);
        user.setNickname("testUser");
        user.setEmail("test@example.com");
        user.setPassword("test_password");
        user.setRole(Role.USER);
    }

    @Test
    void addUser_ShouldReturnUser_WhenUserIsAdded() {

        when(userService.addUser(any(User.class))).thenReturn(new ResponseEntity<>(user, HttpStatus.CREATED));


        ResponseEntity<User> response = userController.addUser(user);


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody());
        verify(userService, times(1)).addUser(user);
    }

    @Test
    void getAllUsers_ShouldReturnUserList_WhenUsersExist() {

        List<User> users = new ArrayList<>();
        users.add(user);
        when(userService.getAllUsers()).thenReturn(users);


        ResponseEntity<List<User>> response = userController.getAllUsers();


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {

        when(userService.getUserById(1)).thenReturn(user);

        ResponseEntity<User> response = userController.getUserById(1);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
        verify(userService, times(1)).getUserById(1);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() {

        when(userService.getUserById(2)).thenReturn(null);


        ResponseEntity<User> response = userController.getUserById(2);


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).getUserById(2);
    }

    @Test
    void deleteUserById_ShouldReturnSuccessMessage_WhenUserIsDeleted() {

        doNothing().when(userService).deleteUserById(1);


        ResponseEntity<String> response = userController.deleteUserById(1);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User with id: 1 has been deleted", response.getBody());
        verify(userService, times(1)).deleteUserById(1);
    }

    @Test
    void deleteUserById_ShouldReturnNotFound_WhenUserDoesNotExist() {

        doThrow(new NoSuchElementException("User not found")).when(userService).deleteUserById(2);


        ResponseEntity<String> response = userController.deleteUserById(2);

        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
        verify(userService, times(1)).deleteUserById(2);
    }
}