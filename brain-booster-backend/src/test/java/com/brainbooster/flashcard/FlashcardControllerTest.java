package com.brainbooster.flashcard;

import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.config.JwtService;
import com.brainbooster.exception.ErrorDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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
        // given
        when(flashcardService.addFlashcard(any(Flashcard.class))).thenReturn(flashcard1);

        // when
        MvcResult result = mockMvc.perform(post("/flashcards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcard1)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Flashcard response = objectMapper.readValue(result.getResponse().getContentAsString(), Flashcard.class);
        assertThat(response.getTerm()).isEqualTo("Test Term");
        assertThat(response.getDefinition()).isEqualTo("Test Definition");
    }

    @Test
    void getAllFlashcards_ShouldReturnListOfFlashcards() throws Exception {
        // given
        when(flashcardService.getAllFlashcards()).thenReturn(List.of(flashcard1, flashcard2));

        // when
        MvcResult result = mockMvc.perform(get("/flashcards"))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

        List<Flashcard> responseList = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responseList).hasSize(2);
        assertThat(responseList.get(0).getTerm()).isEqualTo("Test Term");
        assertThat(responseList.get(1).getTerm()).isEqualTo("Test Term 2");
    }


    @Test
    void getFlashcardById_ShouldReturnFlashcard() throws Exception {
        // given
        when(flashcardService.getFlashcardById(1L)).thenReturn(flashcard1);

        // when
        MvcResult result = mockMvc.perform(get("/flashcards/1"))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

        Flashcard response = objectMapper.readValue(result.getResponse().getContentAsString(), Flashcard.class);
        assertThat(response.getTerm()).isEqualTo("Test Term");
        assertThat(response.getDefinition()).isEqualTo("Test Definition");
    }

    @Test
    void getFlashcardById_shouldReturnNotFound_whenFlashcardDoesNotExist() throws Exception {
        // given
        when(flashcardService.getFlashcardById(1L)).thenThrow(new NoSuchElementException("Not found"));

        // when
        MvcResult result = mockMvc.perform(get("/flashcards/1"))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        ErrorDTO errorResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorDTO.class
        );

        assertThat(errorResponse.message()).isEqualTo("Not found");
        assertThat(errorResponse.status()).isEqualTo("NOT_FOUND");
        assertThat(errorResponse.timestamp()).isNotNull();
    }

    @Test
    void updateFlashcard_ShouldReturnUpdatedFlashcard() throws Exception {
        // given
        when(flashcardService.updateFlashcard(any(Flashcard.class), any(Long.class))).thenReturn(updatedFlashcard);

        // when
        MvcResult result = mockMvc.perform(patch("/flashcards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFlashcard)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

        Flashcard response = objectMapper.readValue(result.getResponse().getContentAsString(), Flashcard.class);
        assertThat(response.getTerm()).isEqualTo("Updated Term");
        assertThat(response.getDefinition()).isEqualTo("Updated Definition");
    }

    @Test
    void deleteFlashcard_ShouldReturnNoContentWithMessage() throws Exception {
        // given
        doNothing().when(flashcardService).deleteFlashcardById(1L);

        // when
        MvcResult result = mockMvc.perform(delete("/flashcards/1"))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        String content = result.getResponse().getContentAsString();
        assertThat(content).isEqualTo("Flashcard with id: 1 has been deleted");
    }


}
