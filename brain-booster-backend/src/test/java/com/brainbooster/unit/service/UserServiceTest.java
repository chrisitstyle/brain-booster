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
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
    void UserService_AddUser_ReturnsSavedUserAndCreatedStatus(){

        //arrange
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        //act
        ResponseEntity<User> savedUser = userService.addUser(user);
        //assert
        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getBody().getUserId()).isEqualTo(1);
        Assertions.assertThat(savedUser.getBody().getNickname()).isEqualTo("testUser");
        Assertions.assertThat(savedUser.getBody().getEmail()).isEqualTo("test@example.com");
        Assertions.assertThat(savedUser.getStatusCode()).isEqualTo(HttpStatus.CREATED);


    }

    @Test
    void UserService_AddUser_ThrowsResponseStatusException_WhenUserEmailAlreadyExist(){

        //arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        //act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.addUser(user);
        });

        //assert
        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        Assertions.assertThat(exception.getReason()).isEqualTo("User with this email already exists");
    }


}
