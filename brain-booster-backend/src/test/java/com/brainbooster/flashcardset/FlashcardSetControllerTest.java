package com.brainbooster.flashcardset;

import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.config.JwtService;
import com.brainbooster.exception.ErrorDTO;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.user.UserDTO;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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

    private final UserDTO userDTO = TestEntities.createUserDTO();

    private final FlashcardSetDTO flashcardSetDTO = TestEntities.createFlashcardSetDTO();

    @Test
    void addFlashcardSetCreationDTO_ShouldReturnFlashcardSetCreationDTO() throws Exception {
        // given
        FlashcardSetCreationDTO flashcardSetCreationDTO = TestEntities.createFlashcardSetCreationDTO();
        String token = "valid_token_test";
        when(jwtService.extractUsername(token)).thenReturn("johndoe@example.com");
        when(flashcardSetService.addFlashcardSet(Mockito.any(FlashcardSetCreationDTO.class)))
                .thenReturn(flashcardSetCreationDTO);

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/flashcard-sets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardSetCreationDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        // then
        FlashcardSetCreationDTO responseDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                FlashcardSetCreationDTO.class
        );

        assertThat(responseDTO.userId()).isEqualTo(1L);
        assertThat(responseDTO.setName()).isEqualTo("test_flashcardset_name");
        assertThat(responseDTO.description()).isEqualTo("test_flashcardset_description");
    }

    @Test
    void getAllFlashcardSets_ShouldReturnListOfFlashcardSetDTO() throws Exception {
        // given
        when(flashcardSetService.getAllFlashcardSets()).thenReturn(List.of(flashcardSetDTO));

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/flashcard-sets"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<FlashcardSetDTO> responseList = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responseList).hasSize(1);
        assertThat(responseList.getFirst().setName()).isEqualTo("test_flashcardset_name");
    }

    @Test
    void getAllFlashcardSetById_ShouldReturnFlashcardSetDTO() throws Exception {

        // given
        when(flashcardSetService.getFlashcardSetById(1L)).thenReturn(flashcardSetDTO);

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/flashcard-sets/1"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        FlashcardSetDTO responseDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                FlashcardSetDTO.class);

        assertThat(responseDTO.setId()).isEqualTo(1L);
        assertThat(responseDTO.setName()).isEqualTo(flashcardSetDTO.setName());
    }

    @Test
    void getAllFlashcardsInSet_ShouldReturnListOfFlashcards() throws Exception {
        // given
        Flashcard flashcard = TestEntities.flashcardBuilder()
                .term("What is Java?")
                .definition("A programming language")
                .build();

        when(flashcardSetService.getAllFlashcardsInSet(1L)).thenReturn(List.of(flashcard));

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/flashcard-sets/1/flashcards"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<Flashcard> responseList = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responseList).hasSize(1);
        assertThat(responseList.getFirst().getTerm()).isEqualTo("What is Java?");
    }

    @Test
    void updateFlashcardSetById_ShouldReturnUpdatedFlashcardSetDTO() throws Exception {
        // given
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

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch("/flashcard-sets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFlashcardSet)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        FlashcardSetDTO responseDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                FlashcardSetDTO.class
        );

        assertThat(responseDTO.setName()).isEqualTo("Updated Set");
        assertThat(responseDTO.description()).isEqualTo("Updated description");
    }

    @Test
    void deleteFlashcardSetById_ShouldReturnNoContent() throws Exception {
        // given
        long setId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.delete("/flashcard-sets/" + setId))
                .andExpect(status().isNoContent());

    }

    @Test
    void getFlashcardSetById_ShouldReturnNotFound_WhenSetDoesNotExist() throws Exception {
        // given
        long nonExistentId = 999L;
        when(flashcardSetService.getFlashcardSetById(nonExistentId))
                .thenThrow(new java.util.NoSuchElementException("FlashcardSet with id: " + nonExistentId + " not found"));

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/flashcard-sets/" + nonExistentId))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        ErrorDTO errorResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorDTO.class
        );

        assertThat(errorResponse.message()).isEqualTo("FlashcardSet with id: 999 not found");
        assertThat(errorResponse.status()).isEqualTo("NOT_FOUND");
    }

    @Test
    void getAllFlashcardsInSet_ShouldReturnNotFound_WhenSetDoesNotExist() throws Exception {
        // given
        long nonExistentId = 123L;
        when(flashcardSetService.getAllFlashcardsInSet(nonExistentId))
                .thenThrow(new NoSuchElementException("FlashcardSet not found"));

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/flashcard-sets/" + nonExistentId + "/flashcards"))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ErrorDTO errorResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);
        assertThat(errorResponse.message()).isEqualTo("FlashcardSet not found");
    }

    @Test
    void updateFlashcardSet_ShouldReturnNotFound_WhenUpdatingNonExistentSet() throws Exception {
        // given
        long nonExistentId = 123L;
        FlashcardSet updateData = new FlashcardSet();
        updateData.setSetName("New Name");

        when(flashcardSetService.updateFlashcardSet(Mockito.any(), Mockito.eq(nonExistentId)))
                .thenThrow(new java.util.NoSuchElementException("FlashcardSet not found"));

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch("/flashcard-sets/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        ErrorDTO errorResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorDTO.class
        );

        assertThat(errorResponse.message()).isEqualTo("FlashcardSet not found");
        assertThat(errorResponse.status()).isEqualTo("NOT_FOUND");
    }

    @Test
    void deleteFlashcardSetById_ShouldReturnNotFound_WhenSetDoesNotExist() throws Exception {
        // given
        long nonExistentId = 123L;
        doThrow(new NoSuchElementException("FlashcardSet not found"))
                .when(flashcardSetService).deleteFlashcardSetById(nonExistentId);

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/flashcard-sets/" + nonExistentId))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ErrorDTO errorResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);
        assertThat(errorResponse.message()).isEqualTo("FlashcardSet not found");
    }
}
