package com.brainbooster.user;


import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.config.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private ObjectMapper objectMapper;


    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO(
                1L,
                "testUser",
                "test@example.com",
                Role.USER,
                LocalDateTime.parse("2024-11-11T00:28:05.738221")
        );
    }

    @Test
    void addUserWithValidJWT_ReturnsCreated() throws Exception {

        String token = "valid_token_test";
        when(jwtService.extractUsername(token)).thenReturn("test@example.com");

        given(userService.addUser(ArgumentMatchers.any())).willReturn(userDTO);

        ResultActions response = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.nickname", CoreMatchers.is(userDTO.nickname())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(userDTO.email())))
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    void getAllUsers_ReturnsUsersDTO() throws Exception {

        when(userService.getAllUsers()).thenReturn(Collections.singletonList(userDTO));

        ResultActions response = mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON));


        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));

    }

    @Test
    void getUserById_ReturnsUserDTO() throws Exception {

        Long userId = 1L;
        when(userService.getUserById(userId)).thenReturn(userDTO);

        ResultActions response = mockMvc.perform(get("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.nickname", CoreMatchers.is(userDTO.nickname())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(userDTO.email())))
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    void updateUser_ReturnsUpdatedUserDTO() throws Exception {

        long userId = 1L;

        when(userService.updateUser(ArgumentMatchers.any(User.class), ArgumentMatchers.eq(userId)))
                .thenReturn(userDTO);

        ResultActions response = mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andDo(MockMvcResultHandlers.print());

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.nickname", CoreMatchers.is(userDTO.nickname())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(userDTO.email())));
    }

    @Test
    void deleteUser_ReturnsString() throws Exception {

        long userId = 1;

        doNothing().when(userService).deleteUserById(userId);

        ResultActions response = mockMvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andExpect(MockMvcResultMatchers.content().string("User with id: " + userId + " has been deleted"));

        verify(userService, times(1)).deleteUserById(userId);

    }


}
