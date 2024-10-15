package com.brainbooster.unit.service;

import com.brainbooster.model.Role;
import com.brainbooster.model.User;
import com.brainbooster.repository.UserRepository;
import com.brainbooster.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void UserService_AddUser_ReturnsSavedUserAndCreatedStatus(){
        //given
       User user = new User();
        user.setUserId(1);
        user.setNickname("testUser");
        user.setEmail("test@example.com");
        user.setPassword("test_password");
        user.setRole(Role.USER);

        //when
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        //then
        ResponseEntity<User> savedUser = userService.addUser(user);

        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getBody().getUserId()).isEqualTo(1);
        Assertions.assertThat(savedUser.getBody().getNickname()).isEqualTo("testUser");
        Assertions.assertThat(savedUser.getBody().getEmail()).isEqualTo("test@example.com");
        Assertions.assertThat(savedUser.getStatusCode()).isEqualTo(HttpStatus.CREATED);


    }


}
