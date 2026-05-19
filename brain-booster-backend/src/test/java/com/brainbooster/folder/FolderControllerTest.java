package com.brainbooster.folder;

import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.folder.dto.FolderCreationDTO;
import com.brainbooster.folder.dto.FolderDTO;
import com.brainbooster.folder.dto.FolderUpdateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static com.brainbooster.utils.TestEntities.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = FolderController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FolderService folderService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createFolder_shouldReturnCreatedFolderDTO() throws Exception {
        FolderCreationDTO creationDTO = createFolderCreationDTO();
        FolderDTO responseDTO = createEmptyFolderDTO();

        when(folderService.createFolder(any(FolderCreationDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.folderId").value(1))
                .andExpect(jsonPath("$.nickname").value("johndoe"))
                .andExpect(jsonPath("$.name").value("test_folder_name"))
                .andExpect(jsonPath("$.description").value("test_folder_description"))
                .andExpect(jsonPath("$.setCount").value(0))
                .andExpect(jsonPath("$.flashcardSets").isArray())
                .andExpect(jsonPath("$.flashcardSets").isEmpty());

        verify(folderService).createFolder(any(FolderCreationDTO.class));
    }

    @Test
    void getAllFolders_shouldReturnFolderDTOs() throws Exception {
        when(folderService.getAllFolders()).thenReturn(List.of(createFolderDTO()));

        mockMvc.perform(get("/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].folderId").value(1))
                .andExpect(jsonPath("$[0].nickname").value("johndoe"))
                .andExpect(jsonPath("$[0].flashcardSets[0].flashcardSetId").value(1))
                .andExpect(jsonPath("$[0].flashcardSets[0].title").value("test_flashcardset_name"));

        verify(folderService).getAllFolders();
    }

    @Test
    void getMyFolders_shouldReturnFolderDTOs() throws Exception {
        when(folderService.getMyFolders()).thenReturn(List.of(createFolderDTO()));

        mockMvc.perform(get("/folders/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].folderId").value(1))
                .andExpect(jsonPath("$[0].nickname").value("johndoe"));

        verify(folderService).getMyFolders();
    }

    @Test
    void getFolderById_shouldReturnFolderDTO() throws Exception {
        when(folderService.getFolderById(1L)).thenReturn(createFolderDTO());

        mockMvc.perform(get("/folders/{folderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folderId").value(1))
                .andExpect(jsonPath("$.setCount").value(1));

        verify(folderService).getFolderById(1L);
    }

    @Test
    void updateFolder_shouldReturnUpdatedFolderDTO() throws Exception {
        FolderUpdateDTO updateDTO = createFolderUpdateDTO();
        FolderDTO updatedDTO = new FolderDTO(
                1L,
                "johndoe",
                "Updated Folder",
                "Updated folder description",
                0L,
                List.of()
        );

        when(folderService.updateFolder(any(Long.class), any(FolderUpdateDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(patch("/folders/{folderId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Folder"))
                .andExpect(jsonPath("$.description").value("Updated folder description"));

        verify(folderService).updateFolder(any(Long.class), any(FolderUpdateDTO.class));
    }

    @Test
    void deleteFolder_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/folders/{folderId}", 1L))
                .andExpect(status().isNoContent());

        verify(folderService).deleteFolder(1L);
    }

    @Test
    void addSetToFolder_shouldReturnFolderWithSet() throws Exception {
        when(folderService.addSetToFolder(1L, 1L)).thenReturn(createFolderDTO());

        mockMvc.perform(post("/folders/{folderId}/sets/{setId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flashcardSets[0].flashcardSetId").value(1))
                .andExpect(jsonPath("$.flashcardSets[0].title").value("test_flashcardset_name"));

        verify(folderService).addSetToFolder(1L, 1L);
    }

    @Test
    void removeSetFromFolder_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/folders/{folderId}/sets/{setId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(folderService).removeSetFromFolder(1L, 1L);
    }
}

