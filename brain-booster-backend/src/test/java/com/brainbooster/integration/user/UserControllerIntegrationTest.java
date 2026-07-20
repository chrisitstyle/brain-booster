package com.brainbooster.integration.user;

import com.brainbooster.config.JwtService;
import com.brainbooster.integration.AbstractIntegrationTest;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import com.brainbooster.user.dto.UserCreationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = "/insert-it-test-users.sql")
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("POST /users - Should persist user in real database when called by Admin")
    void addUser_ShouldSaveUserToPostgres() throws Exception {
        // given
        UserCreationDTO creationDTO = new UserCreationDTO(
                "integrationUser",
                "integration@test.com",
                "securePass123",
                Role.USER);

        // represent the existing admin user from the sql script
        User existingAdmin = User.builder()
                .userId(1L)
                .email("it-admin@test.com")
                .role(Role.ADMIN)
                .build();

        // generate a real token for the logged-in admin
        String realToken = jwtService.generateToken(existingAdmin);

        // when & then
        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("integrationUser"))
                .andExpect(jsonPath("$.email").value("integration@test.com"));


        User savedUser = userRepository.findByEmail("integration@test.com").orElseThrow();
        assertThat(savedUser.getNickname()).isEqualTo("integrationUser");
        assertThat(savedUser.getPassword()).isNotEqualTo("securePass123");
    }

    @Test
    @DisplayName("GET /users/{id} - Should fetch existing user from database when called by Admin")
    void getUserById_ShouldReturnUserFromPostgres() throws Exception {
        // given
        // use the Admin token to ensure authorized access to user profiles
        User existingAdmin = User.builder()
                .userId(1L)
                .email("it-admin@test.com")
                .role(Role.ADMIN)
                .build();

        String realToken = jwtService.generateToken(existingAdmin);

        // when & then
        mockMvc.perform(get("/users/2") // fetching user with id from db
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("it-user"))
                .andExpect(jsonPath("$.email").value("it-user@test.com"));
    }
}