package com.brainbooster.user;


import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.config.JwtService;
import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.exception.ErrorDTO;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private ObjectMapper objectMapper;


    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = TestEntities.createUserDTO();
    }

    @Test
    void addUserWithValidJWT_ReturnsCreated() throws Exception {
        // given
        String token = "valid_token_test";
        when(jwtService.extractUsername(token)).thenReturn("test@example.com");
        given(userService.addUser(ArgumentMatchers.any())).willReturn(userDTO);

        //when
        MvcResult result = mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO responseDTO = objectMapper.readValue(result.getResponse().getContentAsString(), UserDTO.class);

        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.nickname()).isEqualTo(userDTO.nickname());
        assertThat(responseDTO.email()).isEqualTo(userDTO.email());

    }

    @Test
    void getAllUsers_ReturnsUsersDTO() throws Exception {
        // given
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(userDTO));
        // when
        MvcResult result = mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<UserDTO> responseList = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responseList).hasSize(1);
        assertThat(responseList.getFirst().email()).isEqualTo(userDTO.email());

    }

    @Test
    void getUserById_ReturnsUserDTO() throws Exception {
        // given
        long userId = 1L;
        when(userService.getUserById(userId)).thenReturn(userDTO);
        // when
        MvcResult result = mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        UserDTO responseDTO = objectMapper.readValue(result.getResponse().getContentAsString(), UserDTO.class);

        assertThat(responseDTO.nickname()).isEqualTo(userDTO.nickname());
        assertThat(responseDTO.email()).isEqualTo(userDTO.email());

    }

    @Test
    void updateUser_ReturnsUpdatedUserDTO() throws Exception {
        // given
        long userId = 1L;
        when(userService.updateUser(ArgumentMatchers.any(User.class), ArgumentMatchers.eq(userId)))
                .thenReturn(userDTO);

        // when
        MvcResult result = mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        UserDTO responseDTO = objectMapper.readValue(result.getResponse().getContentAsString(), UserDTO.class);

        assertThat(responseDTO.nickname()).isEqualTo(userDTO.nickname());
        assertThat(responseDTO.email()).isEqualTo(userDTO.email());
    }

    @Test
    void deleteUser_ReturnsString() throws Exception {
        // given
        long userId = 1;
        doNothing().when(userService).deleteUserById(userId);

        // when
        MvcResult result = mockMvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        // then
        String content = result.getResponse().getContentAsString();
        assertThat(content).isEqualTo("User with id: " + userId + " has been deleted");

        verify(userService, times(1)).deleteUserById(userId);

    }

    @Test
    void addUser_ShouldReturnUnprocessableEntity_WhenEmailExists() throws Exception {
        // given
        String token = "valid_token";
        when(jwtService.extractUsername(token)).thenReturn("admin@example.com");

        given(userService.addUser(ArgumentMatchers.any()))
                .willThrow(new EmailAlreadyExistsException("User with this email already exists!"));

        // when
        MvcResult result = mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());

        ErrorDTO errorResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorDTO.class
        );

        assertThat(errorResponse.message()).isEqualTo("User with this email already exists!");
        assertThat(errorResponse.status()).isEqualTo("UNPROCESSABLE_CONTENT");
        assertThat(errorResponse.timestamp()).isNotNull();
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // given
        long nonExistentId = 99L;
        when(userService.getUserById(nonExistentId))
                .thenThrow(new java.util.NoSuchElementException("User with this id does not exist"));

        // when
        MvcResult result = mockMvc.perform(get("/users/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        ErrorDTO errorResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorDTO.class
        );

        assertThat(errorResponse.message()).isEqualTo("User with this id does not exist");
        assertThat(errorResponse.status()).isEqualTo("NOT_FOUND");
    }

    @Test
    void deleteUser_ShouldReturnForbidden_WhenAccessDenied() throws Exception {
        // given
        long userId = 1L;
        doThrow(new org.springframework.security.access.AccessDeniedException("You cannot delete yourself or other users"))
                .when(userService).deleteUserById(userId);

        // when
        MvcResult result = mockMvc.perform(delete("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());

        ErrorDTO errorResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorDTO.class
        );

        assertThat(errorResponse.message()).isEqualTo("You cannot delete yourself or other users");
        assertThat(errorResponse.status()).isEqualTo("FORBIDDEN");
    }


}
