package com.brainbooster.flashcard;

import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.config.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = FlashcardController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class FlashcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private FlashcardService flashcardService;

    private final Flashcard flashcard1 = new Flashcard(1L,
            1L,
            "Test Term",
            "Test Definition");

    private final Flashcard flashcard2 = new Flashcard(2L,
            1L,
            "Test Term 2",
            "Test Definition 2");

    Flashcard updatedFlashcard = new Flashcard(1L,
            1L,
            "Updated Term",
            "Updated Definition");


    @Test
    void addFlashcard_ShouldReturnFlashcard() throws Exception {
        when(flashcardService.addFlashcard(any(Flashcard.class))).thenReturn(flashcard1);

        mockMvc.perform(MockMvcRequestBuilders.post("/flashcards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcard1)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.term").value("Test Term"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.definition").value("Test Definition"));

    }

    @Test
    void getAllFlashcards_ShouldReturnListOfFlashcards() throws Exception {
        when(flashcardService.getAllFlashcards()).thenReturn(List.of(flashcard1, flashcard2));

        mockMvc.perform(MockMvcRequestBuilders.get("/flashcards"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].term").value("Test Term"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].definition").value("Test Definition"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].term").value("Test Term 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].definition").value("Test Definition 2"));
    }


    @Test
    void getFlashcardById_ShouldReturnFlashcard() throws Exception {
        when(flashcardService.getFlashcardById(1L)).thenReturn(flashcard1);

        mockMvc.perform(MockMvcRequestBuilders.get("/flashcards/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.term").value("Test Term"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.definition").value("Test Definition"));
    }

    @Test
    void getFlashcardById_shouldReturnNotFound_whenFlashcardDoesNotExist() throws Exception {
        when(flashcardService.getFlashcardById(1L)).thenThrow(new NoSuchElementException("Not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/flashcards/1"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Not found"));
    }

    @Test
    void updateFlashcard_ShouldReturnUpdatedFlashcard() throws Exception {

        when(flashcardService.updateFlashcard(any(Flashcard.class), any(Long.class))).thenReturn(updatedFlashcard);

        mockMvc.perform(MockMvcRequestBuilders.patch("/flashcards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFlashcard)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.term").value("Updated Term"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.definition").value("Updated Definition"));
    }

    @Test
    void deleteFlashcard_ShouldReturnNoContentWithMessage() throws Exception {
        doNothing().when(flashcardService).deleteFlashcardById(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/flashcards/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andExpect(MockMvcResultMatchers.content().string("Flashcard with id: 1 has been deleted"));
    }


}
