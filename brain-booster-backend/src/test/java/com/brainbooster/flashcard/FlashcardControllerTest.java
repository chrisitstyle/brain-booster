package com.brainbooster.flashcard;

import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.config.JwtService;
import com.brainbooster.exception.ErrorDTO;
import com.brainbooster.flashcard.dto.FlashcardCreationDTO;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.dto.FlashcardUpdateDTO;
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
import static org.mockito.ArgumentMatchers.eq;
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

    private final FlashcardDTO flashcardDTO1 = new FlashcardDTO(1L, 1L, "Test Term", "Test Definition");
    private final FlashcardDTO flashcardDTO2 = new FlashcardDTO(2L, 1L, "Test Term 2", "Test Definition 2");
    private final FlashcardDTO updatedFlashcardDTO = new FlashcardDTO(1L, 1L, "Updated Term", "Updated Definition");

    private final FlashcardCreationDTO creationDTO = new FlashcardCreationDTO(1L, "Test Term", "Test Definition");
    private final FlashcardUpdateDTO updateDTO = new FlashcardUpdateDTO("Updated Term", "Updated Definition");


    @Test
    void addFlashcardCreationDTO_ShouldReturnFlashcardDTO() throws Exception {
        // given
        when(flashcardService.addFlashcard(any(FlashcardCreationDTO.class))).thenReturn(flashcardDTO1);

        // when
        MvcResult result = mockMvc.perform(post("/flashcards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        FlashcardDTO response = objectMapper.readValue(
                result.getResponse()
                        .getContentAsString(), FlashcardDTO.class);

        assertThat(response.term()).isEqualTo("Test Term");
        assertThat(response.definition()).isEqualTo("Test Definition");
    }

    @Test
    void getAllFlashcards_ShouldReturnListOfFlashcardsDTOs() throws Exception {
        // given
        when(flashcardService.getAllFlashcards()).thenReturn(List.of(flashcardDTO1, flashcardDTO2));

        // when
        MvcResult result = mockMvc.perform(get("/flashcards"))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

        List<FlashcardDTO> responseList = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responseList).hasSize(2);
        assertThat(responseList.get(0).term()).isEqualTo("Test Term");
        assertThat(responseList.get(1).term()).isEqualTo("Test Term 2");
    }


    @Test
    void getFlashcardById_ShouldReturnFlashcardDTO() throws Exception {
        // given
        when(flashcardService.getFlashcardById(1L)).thenReturn(flashcardDTO1);

        // when
        MvcResult result = mockMvc.perform(get("/flashcards/1"))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

        FlashcardDTO response = objectMapper.readValue(
                result.getResponse()
                        .getContentAsString(), FlashcardDTO.class);

        assertThat(response.term()).isEqualTo("Test Term");
        assertThat(response.definition()).isEqualTo("Test Definition");
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
    void updateFlashcard_ShouldReturnUpdatedFlashcardDTO() throws Exception {
        // given
        when(flashcardService.updateFlashcard(any(FlashcardUpdateDTO.class), eq(1L))).thenReturn(updatedFlashcardDTO);

        // when
        MvcResult result = mockMvc.perform(patch("/flashcards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

        FlashcardDTO response = objectMapper.readValue(
                result.getResponse()
                        .getContentAsString(), FlashcardDTO.class);
        assertThat(response.term()).isEqualTo("Updated Term");
        assertThat(response.definition()).isEqualTo("Updated Definition");
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
