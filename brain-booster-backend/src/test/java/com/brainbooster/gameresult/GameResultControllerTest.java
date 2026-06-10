package com.brainbooster.gameresult;

import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.gameresult.dto.GameResultDTO;
import com.brainbooster.gameresult.dto.SaveGameResultRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static com.brainbooster.utils.TestEntities.createGameResultDTO;
import static com.brainbooster.utils.TestEntities.createSaveGameResultRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GameResultController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class GameResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameResultService gameResultService;

    /* Required because JwtAuthenticationFilter is part of the application security configuration
 and the MVC slice test does not load all of its dependencies */
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldSaveGameResult() throws Exception {
        SaveGameResultRequest request = createSaveGameResultRequest(
                11L,
                GameMode.MULTIPLE_CHOICE,
                8,
                10,
                120
        );

        GameResultDTO response = createGameResultDTO(
                1L,
                2L,
                11L,
                GameMode.MULTIPLE_CHOICE,
                8,
                10,
                120
        );

        when(gameResultService.saveGameResult(any(SaveGameResultRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/game-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultId").value(1L))
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.setId").value(11L))
                .andExpect(jsonPath("$.mode").value("multiple-choice"))
                .andExpect(jsonPath("$.score").value(8))
                .andExpect(jsonPath("$.totalQuestions").value(10))
                .andExpect(jsonPath("$.durationSeconds").value(120));

        ArgumentCaptor<SaveGameResultRequest> requestCaptor =
                ArgumentCaptor.forClass(SaveGameResultRequest.class);

        verify(gameResultService).saveGameResult(requestCaptor.capture());

        assertThat(requestCaptor.getValue()).isEqualTo(request);
    }

    @Test
    void shouldReturnBadRequestWhenSavingGameResultWithInvalidScore() throws Exception {
        SaveGameResultRequest request = createSaveGameResultRequest(
                11L,
                GameMode.MATCHING,
                -1,
                10,
                null
        );

        mockMvc.perform(post("/game-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameResultService);
    }

    @Test
    void shouldReturnBadRequestWhenSavingGameResultWithInvalidTotalQuestions() throws Exception {
        SaveGameResultRequest request = createSaveGameResultRequest(
                11L,
                GameMode.WRITTEN,
                0,
                0,
                null
        );

        mockMvc.perform(post("/game-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameResultService);
    }

    @Test
    void shouldReturnMyGameResultsWithoutSetFilter() throws Exception {
        GameResultDTO response = createGameResultDTO(
                1L,
                2L,
                11L,
                GameMode.MULTIPLE_CHOICE,
                8,
                10,
                null
        );

        when(gameResultService.getMyGameResults(null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/game-results/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultId").value(1L))
                .andExpect(jsonPath("$[0].userId").value(2L))
                .andExpect(jsonPath("$[0].setId").value(11L))
                .andExpect(jsonPath("$[0].mode").value("multiple-choice"))
                .andExpect(jsonPath("$[0].score").value(8))
                .andExpect(jsonPath("$[0].totalQuestions").value(10));

        verify(gameResultService).getMyGameResults(null);
    }

    @Test
    void shouldReturnMyGameResultsWithSetFilter() throws Exception {
        GameResultDTO response = createGameResultDTO(
                1L,
                2L,
                11L,
                GameMode.WRITTEN,
                7,
                10,
                null
        );

        when(gameResultService.getMyGameResults(11L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/game-results/me")
                        .param("setId", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultId").value(1L))
                .andExpect(jsonPath("$[0].userId").value(2L))
                .andExpect(jsonPath("$[0].setId").value(11L))
                .andExpect(jsonPath("$[0].mode").value("written"))
                .andExpect(jsonPath("$[0].score").value(7))
                .andExpect(jsonPath("$[0].totalQuestions").value(10));

        verify(gameResultService).getMyGameResults(11L);
    }

    @Test
    void shouldReturnAllGameResultsWithoutSetFilter() throws Exception {
        GameResultDTO response = createGameResultDTO(
                1L,
                2L,
                11L,
                GameMode.MATCHING,
                5,
                6,
                null
        );

        when(gameResultService.getAllGameResults(null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/game-results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultId").value(1L))
                .andExpect(jsonPath("$[0].userId").value(2L))
                .andExpect(jsonPath("$[0].setId").value(11L))
                .andExpect(jsonPath("$[0].mode").value("matching"))
                .andExpect(jsonPath("$[0].score").value(5))
                .andExpect(jsonPath("$[0].totalQuestions").value(6));

        verify(gameResultService).getAllGameResults(null);
    }

    @Test
    void shouldReturnAllGameResultsWithSetFilter() throws Exception {
        GameResultDTO response = createGameResultDTO(
                1L,
                2L,
                11L,
                GameMode.CUSTOM_TEST,
                9,
                10,
                null
        );

        when(gameResultService.getAllGameResults(11L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/game-results")
                        .param("setId", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultId").value(1L))
                .andExpect(jsonPath("$[0].userId").value(2L))
                .andExpect(jsonPath("$[0].setId").value(11L))
                .andExpect(jsonPath("$[0].mode").value("custom-test"))
                .andExpect(jsonPath("$[0].score").value(9))
                .andExpect(jsonPath("$[0].totalQuestions").value(10));

        verify(gameResultService).getAllGameResults(11L);
    }

    @Test
    void shouldReturnGameResultById() throws Exception {
        GameResultDTO response = createGameResultDTO(
                1L,
                2L,
                11L,
                GameMode.MULTIPLE_CHOICE,
                8,
                10,
                null
        );

        when(gameResultService.getGameResultById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/game-results/{resultId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultId").value(1L))
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.setId").value(11L))
                .andExpect(jsonPath("$.mode").value("multiple-choice"))
                .andExpect(jsonPath("$.score").value(8))
                .andExpect(jsonPath("$.totalQuestions").value(10));

        verify(gameResultService).getGameResultById(1L);
    }

    @Test
    void shouldDeleteGameResult() throws Exception {
        mockMvc.perform(delete("/game-results/{resultId}", 1L))
                .andExpect(status().isNoContent());

        verify(gameResultService).deleteGameResult(1L);
    }
}