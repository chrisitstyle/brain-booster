package com.brainbooster.gameresult;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.gameresult.dto.GameResultDTO;
import com.brainbooster.gameresult.dto.SaveGameResultRequest;
import com.brainbooster.gameresult.mapper.GameResultMapper;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.brainbooster.utils.TestEntities.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameResultServiceTest {

    @Mock
    private GameResultRepository gameResultRepository;

    @Mock
    private FlashcardSetRepository flashcardSetRepository;

    @Mock
    private GameResultMapper gameResultMapper;

    @InjectMocks
    private GameResultService gameResultService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateGameResultWhenResultDoesNotExist() {
        User authUser = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, authUser);

        authenticate(authUser);

        SaveGameResultRequest request = createSaveGameResultRequest(
                11L,
                GameMode.MULTIPLE_CHOICE,
                8,
                10,
                120
        );

        GameResultDTO expectedDto = createGameResultDTO(
                1L,
                2L,
                11L,
                GameMode.MULTIPLE_CHOICE,
                8,
                10,
                120
        );

        when(flashcardSetRepository.findById(11L))
                .thenReturn(Optional.of(flashcardSet));

        when(gameResultRepository.findByUser_UserIdAndSet_SetIdAndMode(
                2L,
                11L,
                GameMode.MULTIPLE_CHOICE
        )).thenReturn(Optional.empty());

        when(gameResultRepository.save(any(GameResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(gameResultMapper.toDto(any(GameResult.class)))
                .thenReturn(expectedDto);

        GameResultDTO result = gameResultService.saveGameResult(request);

        assertThat(result).isEqualTo(expectedDto);

        ArgumentCaptor<GameResult> gameResultCaptor =
                ArgumentCaptor.forClass(GameResult.class);

        verify(gameResultRepository).save(gameResultCaptor.capture());

        GameResult savedGameResult = gameResultCaptor.getValue();

        assertThat(savedGameResult.getUser()).isEqualTo(authUser);
        assertThat(savedGameResult.getSet()).isEqualTo(flashcardSet);
        assertThat(savedGameResult.getMode()).isEqualTo(GameMode.MULTIPLE_CHOICE);
        assertThat(savedGameResult.getScore()).isEqualTo(8);
        assertThat(savedGameResult.getTotalQuestions()).isEqualTo(10);
        assertThat(savedGameResult.getDurationSeconds()).isEqualTo(120);
        assertThat(savedGameResult.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldUpdateGameResultWhenResultAlreadyExists() {
        User authUser = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, authUser);

        authenticate(authUser);

        GameResult existingGameResult = createGameResult(
                authUser,
                flashcardSet,
                GameMode.WRITTEN
        );

        SaveGameResultRequest request = createSaveGameResultRequest(
                11L,
                GameMode.WRITTEN,
                9,
                10,
                120
        );

        GameResultDTO expectedDto = createGameResultDTO(
                1L,
                2L,
                11L,
                GameMode.WRITTEN,
                9,
                10,
                120
        );

        when(flashcardSetRepository.findById(11L))
                .thenReturn(Optional.of(flashcardSet));

        when(gameResultRepository.findByUser_UserIdAndSet_SetIdAndMode(
                2L,
                11L,
                GameMode.WRITTEN
        )).thenReturn(Optional.of(existingGameResult));

        when(gameResultRepository.save(existingGameResult))
                .thenReturn(existingGameResult);

        when(gameResultMapper.toDto(existingGameResult))
                .thenReturn(expectedDto);

        GameResultDTO result = gameResultService.saveGameResult(request);

        assertThat(result).isEqualTo(expectedDto);

        assertThat(existingGameResult.getScore()).isEqualTo(9);
        assertThat(existingGameResult.getTotalQuestions()).isEqualTo(10);
        assertThat(existingGameResult.getDurationSeconds()).isEqualTo(120);
        assertThat(existingGameResult.getCompletedAt()).isNotNull();

        verify(gameResultRepository).save(existingGameResult);
    }

    @Test
    void shouldThrowExceptionWhenScoreIsGreaterThanTotalQuestions() {
        User authUser = createUser(2L, Role.USER);

        authenticate(authUser);

        SaveGameResultRequest request = createSaveGameResultRequest(
                11L,
                GameMode.MATCHING,
                11,
                10,
                null
        );

        assertThatThrownBy(() -> gameResultService.saveGameResult(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Score cannot be greater than total questions.");

        verifyNoInteractions(flashcardSetRepository);
        verifyNoInteractions(gameResultRepository);
        verifyNoInteractions(gameResultMapper);
    }

    @Test
    void shouldThrowExceptionWhenFlashcardSetDoesNotExist() {
        User authUser = createUser(2L, Role.USER);

        authenticate(authUser);

        SaveGameResultRequest request = createSaveGameResultRequest(
                99L,
                GameMode.CUSTOM_TEST,
                5,
                10,
                null
        );

        when(flashcardSetRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameResultService.saveGameResult(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("FlashcardSet with id: 99 not found");

        verify(gameResultRepository, never()).save(any());
    }

    @Test
    void shouldReturnMyGameResultsWithoutSetFilter() {
        User authUser = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, authUser);
        GameResult gameResult = createGameResult(
                authUser,
                flashcardSet,
                GameMode.MULTIPLE_CHOICE
        );
        GameResultDTO dto = createGameResultDTO(gameResult);

        authenticate(authUser);

        when(gameResultRepository.findByUser_UserIdOrderByCompletedAtDesc(2L))
                .thenReturn(List.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        List<GameResultDTO> results = gameResultService.getMyGameResults(null);

        assertThat(results).containsExactly(dto);

        verify(gameResultRepository)
                .findByUser_UserIdOrderByCompletedAtDesc(2L);

        verify(gameResultRepository, never())
                .findByUser_UserIdAndSet_SetIdOrderByCompletedAtDesc(
                        anyLong(),
                        anyLong()
                );
    }

    @Test
    void shouldReturnMyGameResultsWithSetFilter() {
        User authUser = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, authUser);
        GameResult gameResult = createGameResult(
                authUser,
                flashcardSet,
                GameMode.WRITTEN
        );
        GameResultDTO dto = createGameResultDTO(gameResult);

        authenticate(authUser);

        when(gameResultRepository.findByUser_UserIdAndSet_SetIdOrderByCompletedAtDesc(
                2L,
                11L
        )).thenReturn(List.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        List<GameResultDTO> results = gameResultService.getMyGameResults(11L);

        assertThat(results).containsExactly(dto);

        verify(gameResultRepository)
                .findByUser_UserIdAndSet_SetIdOrderByCompletedAtDesc(2L, 11L);

        verify(gameResultRepository, never())
                .findByUser_UserIdOrderByCompletedAtDesc(anyLong());
    }

    @Test
    void shouldReturnAllGameResultsForAdmin() {
        User admin = createUser(1L, Role.ADMIN);
        User owner = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);
        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.MATCHING
        );
        GameResultDTO dto = createGameResultDTO(gameResult);

        authenticate(admin);

        when(gameResultRepository.findAllByOrderByCompletedAtDesc())
                .thenReturn(List.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        List<GameResultDTO> results = gameResultService.getAllGameResults(null);

        assertThat(results).containsExactly(dto);

        verify(gameResultRepository).findAllByOrderByCompletedAtDesc();
    }

    @Test
    void shouldReturnAllGameResultsForSetForAdmin() {
        User admin = createUser(1L, Role.ADMIN);
        User owner = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);
        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.CUSTOM_TEST
        );
        GameResultDTO dto = createGameResultDTO(gameResult);

        authenticate(admin);

        when(gameResultRepository.findBySet_SetIdOrderByCompletedAtDesc(11L))
                .thenReturn(List.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        List<GameResultDTO> results = gameResultService.getAllGameResults(11L);

        assertThat(results).containsExactly(dto);

        verify(gameResultRepository).findBySet_SetIdOrderByCompletedAtDesc(11L);
    }

    @Test
    void shouldThrowAccessDeniedWhenNonAdminGetsAllGameResults() {
        User user = createUser(2L, Role.USER);

        authenticate(user);

        assertThatThrownBy(() -> gameResultService.getAllGameResults(null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only admins can access this resource.");

        verifyNoInteractions(gameResultRepository);
        verifyNoInteractions(gameResultMapper);
    }

    @Test
    void shouldReturnGameResultByIdForOwner() {
        User owner = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);
        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.CUSTOM_TEST
        );
        GameResultDTO dto = createGameResultDTO(gameResult);

        authenticate(owner);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        GameResultDTO result = gameResultService.getGameResultById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void shouldReturnGameResultByIdForAdmin() {
        User admin = createUser(1L, Role.ADMIN);
        User owner = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);
        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.CUSTOM_TEST
        );
        GameResultDTO dto = createGameResultDTO(gameResult);

        authenticate(admin);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        GameResultDTO result = gameResultService.getGameResultById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void shouldThrowExceptionWhenGameResultByIdDoesNotExist() {
        User authUser = createUser(2L, Role.USER);

        authenticate(authUser);

        when(gameResultRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameResultService.getGameResultById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("GameResult with id: 99 not found");

        verify(gameResultMapper, never()).toDto(any());
    }

    @Test
    void shouldThrowAccessDeniedWhenUserGetsSomeoneElsesGameResult() {
        User authUser = createUser(3L, Role.USER);
        User owner = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);
        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.WRITTEN
        );

        authenticate(authUser);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        assertThatThrownBy(() -> gameResultService.getGameResultById(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not allowed to access this game result.");

        verify(gameResultMapper, never()).toDto(any());
    }

    @Test
    void shouldDeleteGameResultForOwner() {
        User owner = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);
        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.MATCHING
        );

        authenticate(owner);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        gameResultService.deleteGameResult(1L);

        verify(gameResultRepository).delete(gameResult);
    }

    @Test
    void shouldDeleteGameResultForAdmin() {
        User admin = createUser(1L, Role.ADMIN);
        User owner = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);
        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.MATCHING
        );

        authenticate(admin);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        gameResultService.deleteGameResult(1L);

        verify(gameResultRepository).delete(gameResult);
    }

    @Test
    void shouldThrowAccessDeniedWhenUserDeletesSomeoneElsesGameResult() {
        User authUser = createUser(3L, Role.USER);
        User owner = createUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);
        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.MATCHING
        );

        authenticate(authUser);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        assertThatThrownBy(() -> gameResultService.deleteGameResult(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not allowed to delete this game result.");

        verify(gameResultRepository, never()).delete(any());
    }

    private void authenticate(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
