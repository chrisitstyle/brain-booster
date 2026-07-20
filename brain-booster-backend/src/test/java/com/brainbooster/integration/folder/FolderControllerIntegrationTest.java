package com.brainbooster.integration.folder;

import com.brainbooster.folder.Folder;
import com.brainbooster.folder.FolderRepository;
import com.brainbooster.integration.AbstractIntegrationTest;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FolderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("GET /users/{nickname}/folders - Should return folders array for existing user")
    void getFoldersByNickname_ShouldReturnMappedFoldersFromDb() throws Exception {
        // given
        User user = User.builder()
                .nickname("alex99")
                .email("alex@test.com")
                .password("password")
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);

        Folder folder = Folder.builder()
                .name("Alex English Folder")
                .description("Vocabulary sets")
                .user(user)
                .createdAt(Instant.now())
                .build();
        folderRepository.save(folder);

        // when & then
        mockMvc.perform(get("/users/alex99/folders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Alex English Folder"))
                .andExpect(jsonPath("$[0].nickname").value("alex99"));
    }
}
