package com.brainbooster.integration.user;

import com.brainbooster.config.JwtService;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.folder.Folder;
import com.brainbooster.folder.FolderRepository;
import com.brainbooster.integration.AbstractIntegrationTest;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import com.brainbooster.user.dto.UserCreationDTO;
import com.brainbooster.user.dto.UserUpdateDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private FolderRepository folderRepository;

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private JwtService jwtService;

    // --- REGISTRATION AND PROFILE TESTS ---

    @Test
    @DisplayName("POST /users - Should persist user in real database when called by Admin")
    void addUser_ShouldSaveUserToPostgres() throws Exception {
        // given
        UserCreationDTO creationDTO = new UserCreationDTO(
                "integrationUser",
                "integration@test.com",
                "securePass123",
                Role.USER);

        User existingAdmin = User.builder()
                .userId(1L)
                .email("it-admin@test.com")
                .role(Role.ADMIN)
                .build();

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
    @DisplayName("GET /users/me - Should return authenticated user details")
    void getCurrentUser_ShouldReturnCurrentUserInfo() throws Exception {
        // given
        User existingUser = User.builder()
                .userId(2L)
                .email("it-user@test.com")
                .role(Role.USER)
                .build();

        String realToken = jwtService.generateToken(existingUser);

        // when & then
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("it-user"))
                .andExpect(jsonPath("$.email").value("it-user@test.com"));
    }

    @Test
    @DisplayName("GET /users - Should return all users when called by Admin")
    void getAllUsers_ShouldReturnUserListForAdmin() throws Exception {
        // given
        User existingAdmin = User.builder()
                .userId(1L)
                .email("it-admin@test.com")
                .role(Role.ADMIN)
                .build();

        String realToken = jwtService.generateToken(existingAdmin);

        // when & then
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)) // it-admin and it-user
                .andExpect(jsonPath("$[0].nickname").value("it-admin"))
                .andExpect(jsonPath("$[1].nickname").value("it-user"));
    }

    @Test
    @DisplayName("GET /users/{id} - Should fetch existing user from database when called by Admin")
    void getUserById_ShouldReturnUserFromPostgres() throws Exception {
        // given
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

    // --- UPDATE AND DELETE TESTS ---

    @Test
    @DisplayName("PUT /users/{id} - Should update user data when called by Admin")
    void updateUser_ShouldModifyUserInDb() throws Exception {
        // given
        UserUpdateDTO updateDTO = new UserUpdateDTO("newNickname", "updated-user@test.com", "newPassword123", Role.USER);
        User existingAdmin = User.builder()
                .userId(1L)
                .email("it-admin@test.com")
                .role(Role.ADMIN)
                .build();

        String realToken = jwtService.generateToken(existingAdmin);

        // when & then
        mockMvc.perform(put("/users/2")
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("newNickname"))
                .andExpect(jsonPath("$.email").value("updated-user@test.com"));

        User updatedUser = userRepository.findById(2L).orElseThrow();
        assertThat(updatedUser.getNickname()).isEqualTo("newNickname");
        assertThat(updatedUser.getEmail()).isEqualTo("updated-user@test.com");
    }

    @Test
    @DisplayName("DELETE /users/{id} - Should remove user and return 204 No Content")
    void deleteUserById_ShouldRemoveUserFromDb() throws Exception {
        // given
        User existingAdmin = User.builder()
                .userId(1L)
                .email("it-admin@test.com")
                .role(Role.ADMIN)
                .build();

        String realToken = jwtService.generateToken(existingAdmin);

        // when & then
        mockMvc.perform(delete("/users/2")
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(2L)).isEmpty();
    }

    // --- RELATION TESTS (FOLDERS AND FLASHCARD SETS) ---

    @Test
    @DisplayName("GET /users/{id}/flashcard-sets - Should return public flashcard sets for user")
    void getAllFlashcardSetsByUserId_ShouldReturnSetsArray() throws Exception {
        // given
        User dbUser = userRepository.findById(2L).orElseThrow();
        FlashcardSet testSet = FlashcardSet.builder()
                .setName("IT Vocabulary")
                .description("English terms")
                .user(dbUser)
                .createdAt(Instant.now())
                .build();
        flashcardSetRepository.save(testSet);

        // when & then
        mockMvc.perform(get("/users/2/flashcard-sets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].setName").value("IT Vocabulary"));
    }

    @Test
    @DisplayName("GET /users/nickname/{nickname}/flashcard-sets - Should return sets by nickname")
    void getAllFlashcardSetsByNickname_ShouldReturnSetsArray() throws Exception {
        // given
        User dbUser = userRepository.findById(2L).orElseThrow();
        FlashcardSet testSet = FlashcardSet.builder()
                .setName("Spring Boot Advanced")
                .description("Pro features")
                .user(dbUser)
                .createdAt(Instant.now())
                .build();
        flashcardSetRepository.save(testSet);

        // when & then
        mockMvc.perform(get("/users/nickname/it-user/flashcard-sets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].setName").value("Spring Boot Advanced"));
    }

    @Test
    @DisplayName("GET /users/{nickname}/folders - Should return public folders by nickname")
    void getFoldersByNickname_ShouldReturnFoldersArray() throws Exception {
        // given
        User dbUser = userRepository.findById(2L).orElseThrow();
        Folder testFolder = Folder.builder()
                .name("Backend Development")
                .description("All backend sets")
                .user(dbUser)
                .createdAt(Instant.now())
                .build();
        folderRepository.save(testFolder);

        // when & then
        mockMvc.perform(get("/users/it-user/folders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Backend Development"))
                .andExpect(jsonPath("$[0].nickname").value("it-user"));
    }
}