package com.brainbooster.integration.folder;

import com.brainbooster.config.JwtService;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.folder.Folder;
import com.brainbooster.folder.FolderRepository;
import com.brainbooster.folder.dto.FolderCreationDTO;
import com.brainbooster.folder.dto.FolderUpdateDTO;
import com.brainbooster.integration.AbstractIntegrationTest;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
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
class FolderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private JwtService jwtService;

    // --- FOLDER TESTS ---

    @Test
    @DisplayName("POST /folders - Should create folder for authenticated user")
    void createFolder_ShouldPersistFolderInDb() throws Exception {
        // given
        User dbUser = userRepository.findById(2L).orElseThrow();
        String realToken = jwtService.generateToken(dbUser);

        FolderCreationDTO creationDTO = new FolderCreationDTO("My English Words", "Folder for C1 level");

        // when & then
        mockMvc.perform(post("/folders")
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My English Words"))
                .andExpect(jsonPath("$.description").value("Folder for C1 level"))
                .andExpect(jsonPath("$.nickname").value("it-user1"));
    }

    @Test
    @DisplayName("GET /folders/me - Should return folders owned by authenticated user")
    void getMyFolders_ShouldReturnOnlyUserFolders() throws Exception {
        // given
        User user1 = userRepository.findById(1L).orElseThrow(); // admin
        User user2 = userRepository.findById(2L).orElseThrow(); // regular user

        String realTokenUser2 = jwtService.generateToken(user2);

        // create a folder for user 1 (should NOT be returned)
        Folder folder1 = Folder.builder()
                .name("Admin's Secret Folder")
                .description("Admin stuff")
                .user(user1)
                .createdAt(Instant.now())
                .build();
        folderRepository.save(folder1);

        // create a folder for user 2 (SHOULD be returned)
        Folder folder2 = Folder.builder()
                .name("User's Study Folder")
                .description("User stuff")
                .user(user2)
                .createdAt(Instant.now())
                .build();
        folderRepository.save(folder2);

        // when & then
        mockMvc.perform(get("/folders/me")
                        .header("Authorization", "Bearer " + realTokenUser2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("User's Study Folder"));
    }

    @Test
    @DisplayName("PATCH /folders/{id} - Should update folder details")
    void updateFolder_ShouldModifyFolderInDb() throws Exception {
        // given
        User dbUser = userRepository.findById(2L).orElseThrow();
        String realToken = jwtService.generateToken(dbUser);

        Folder folder = Folder.builder()
                .name("Old Name")
                .description("Old Description")
                .user(dbUser)
                .createdAt(Instant.now())
                .build();
        Folder savedFolder = folderRepository.save(folder);

        FolderUpdateDTO updateDTO = new FolderUpdateDTO("New Awesome Name", "Updated Description");

        // when & then
        mockMvc.perform(patch("/folders/" + savedFolder.getFolderId())
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Awesome Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    @DisplayName("DELETE /folders/{id} - Should remove folder and return 204")
    void deleteFolder_ShouldRemoveFolderFromDb() throws Exception {
        // given
        User dbUser = userRepository.findById(2L).orElseThrow();
        String realToken = jwtService.generateToken(dbUser);

        Folder folder = Folder.builder()
                .name("To Be Deleted")
                .description("Desc")
                .user(dbUser)
                .createdAt(Instant.now())
                .build();
        Folder savedFolder = folderRepository.save(folder);

        // when & then
        mockMvc.perform(delete("/folders/" + savedFolder.getFolderId())
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(folderRepository.findById(savedFolder.getFolderId())).isEmpty();
    }

    // --- RELATION TESTS (FOLDER <-> FLASHCARD SET) ---

    @Test
    @DisplayName("POST /folders/{folderId}/sets/{setId} - Should link flashcard set to folder")
    void addSetToFolder_ShouldCreateRelation() throws Exception {
        // given
        User dbUser = userRepository.findById(2L).orElseThrow();
        String realToken = jwtService.generateToken(dbUser);

        Folder folder = Folder.builder()
                .name("Main Folder")
                .description("My main folder")
                .user(dbUser)
                .createdAt(Instant.now())
                .build();
        Folder savedFolder = folderRepository.save(folder);

        FlashcardSet flashcardSet = FlashcardSet.builder()
                .setName("Programming Concepts")
                .description("Basic concepts")
                .user(dbUser)
                .createdAt(Instant.now())
                .build();
        FlashcardSet savedSet = flashcardSetRepository.save(flashcardSet);

        // when & then
        mockMvc.perform(post("/folders/" + savedFolder.getFolderId() + "/sets/" + savedSet.getSetId())
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /folders/{folderId}/sets/{setId} - Should remove flashcard set from folder")
    void removeSetFromFolder_ShouldDeleteRelation() throws Exception {
        // given
        User dbUser = userRepository.findById(2L).orElseThrow();
        String realToken = jwtService.generateToken(dbUser);

        Folder folder = Folder.builder()
                .name("Main Folder")
                .description("My main folder")
                .user(dbUser)
                .createdAt(Instant.now())
                .build();

        FlashcardSet flashcardSet = FlashcardSet.builder()
                .setName("Programming Concepts")
                .description("Basic concepts")
                .user(dbUser)
                .createdAt(Instant.now())
                .build();

        FlashcardSet savedSet = flashcardSetRepository.save(flashcardSet);

        folder.getFlashcardSets().add(savedSet);
        Folder savedFolder = folderRepository.save(folder);

        // when & then
        mockMvc.perform(delete("/folders/" + savedFolder.getFolderId() + "/sets/" + savedSet.getSetId())
                        .header("Authorization", "Bearer " + realToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); // 204
    }
}