package com.brainbooster.flashcardset;

import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.config.JwtService;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.user.Role;
import com.brainbooster.user.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FlashcardSetController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class FlashcardSetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlashcardSetService flashcardSetService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private final UserDTO userDTO = new UserDTO(1L,
            "testuser",
            "test@example.com",
            Role.USER, LocalDateTime.parse("2025-06-02T00:28:05.738221"));

    private final FlashcardSetDTO flashcardSetDTO = new FlashcardSetDTO(
            1L, userDTO, "Test Set", "Test description",
            LocalDateTime.parse("2025-06-02T00:28:05.738221")
    );

    @Test
    void addFlashcardSetCreationDTO_ShouldReturnFlashcardSetCreationDTO() throws Exception {
        // Given
        FlashcardSetCreationDTO flashcardSetCreationDTO = new FlashcardSetCreationDTO(
                1L,
                "Test Set",
                "Test description"
        );

        String token = "valid_token_test";
        when(jwtService.extractUsername(token)).thenReturn("test@example.com");

        when(flashcardSetService.addFlashcardSet(Mockito.any(FlashcardSetCreationDTO.class)))
                .thenReturn(flashcardSetCreationDTO);

        // when + then
        mockMvc.perform(MockMvcRequestBuilders.post("/flashcard-sets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardSetCreationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.setName").value("Test Set"))
                .andExpect(jsonPath("$.description").value("Test description"));
    }

    @Test
    void getAllFlashcardSets_ShouldReturnListOfFlashcardSetDTO() throws Exception {

        when(flashcardSetService.getAllFlashcardSets()).thenReturn(List.of(flashcardSetDTO));

        mockMvc.perform(MockMvcRequestBuilders.get("/flashcard-sets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].setName").value("Test Set"));
    }
    @Test
    void getAllFlashcardSetById_ShouldReturnFlashcardSetDTO() throws Exception {

        when(flashcardSetService.getFlashcardSetById(1L)).thenReturn(flashcardSetDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/flashcard-sets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setId").value(1L));
    }

    @Test
    void getAllFlashcardsInSet_ShouldReturnListOfFlashcards() throws Exception {
        Flashcard flashcard = new Flashcard();
        flashcard.setTerm("What is Java?");
        flashcard.setDefinition("A programming language");

        when(flashcardSetService.getAllFlashcardsInSet(1L)).thenReturn(List.of(flashcard));

        mockMvc.perform(MockMvcRequestBuilders.get("/flashcard-sets/1/flashcards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].term").value("What is Java?"));
    }

    @Test
    void updateFlashcardSetById_ShouldReturnUpdatedFlashcardSetDTO() throws Exception {

        FlashcardSet updatedFlashcardSet = new FlashcardSet();
        updatedFlashcardSet.setSetName("Updated Set");
        updatedFlashcardSet.setDescription("Updated description");


        FlashcardSetDTO updatedDTO = new FlashcardSetDTO(
                1L,
                userDTO,
                "Updated Set",
                "Updated description",
                LocalDateTime.parse("2025-06-02T00:28:05.738221")
        );

        when(flashcardSetService.updateFlashcardSet(Mockito.any(), Mockito.eq(1L)))
                .thenReturn(updatedDTO);


        mockMvc.perform(MockMvcRequestBuilders.patch("/flashcard-sets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFlashcardSet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setName").value("Updated Set"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void deleteFlashcardSetById_ShouldReturnNoContent() throws Exception {

        long setId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.delete("/flashcard-sets/" + setId))
                .andExpect(status().isNoContent());

    }
}
