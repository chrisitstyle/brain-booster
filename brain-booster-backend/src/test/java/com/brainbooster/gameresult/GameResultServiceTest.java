package com.brainbooster.gameresult;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.gameresult.attempt.GameAttemptRecorder;
import com.brainbooster.gameresult.dto.GameResultDTO;
import com.brainbooster.gameresult.dto.SaveGameResultRequest;
import com.brainbooster.gameresult.mapper.GameResultMapper;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.security.authorization.AdminPolicy;
import com.brainbooster.security.authorization.OwnerOrAdminPolicy;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
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
    private GameAttemptRecorder gameAttemptRecorder;
    @Mock
    private GameResultMapper gameResultMapper;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private UserRepository userRepository;

    private final OwnerOrAdminPolicy ownerOrAdminPolicy = new OwnerOrAdminPolicy();

    private final AdminPolicy adminPolicy = new AdminPolicy();

    private GameResultService gameResultService;

    @BeforeEach
    void setUp() {
        gameResultService = new GameResultService(
                gameResultRepository,
                flashcardSetRepository,
                gameAttemptRecorder,
                gameResultMapper,
                currentUserProvider,
                ownerOrAdminPolicy,
                adminPolicy,
                userRepository
        );
    }

    @Test
    void shouldCreateGameResultWhenResultDoesNotExist() {
        User user = createUser(2L, Role.USER);
        AuthenticatedUser authenticatedUser = createAuthenticatedUser(2L, Role.USER);

        FlashcardSet flashcardSet =
                createFlashcardSet(11L, user);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(userRepository.getReferenceById(2L))
                .thenReturn(user);

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

        ArgumentCaptor<GameResult> gameResultCaptor = ArgumentCaptor.forClass(GameResult.class);

        verify(gameResultRepository)
                .save(gameResultCaptor.capture());

        GameResult savedGameResult = gameResultCaptor.getValue();

        assertThat(savedGameResult.getUser())
                .isEqualTo(user);

        assertThat(savedGameResult.getSet())
                .isEqualTo(flashcardSet);

        assertThat(savedGameResult.getMode())
                .isEqualTo(GameMode.MULTIPLE_CHOICE);

        assertThat(savedGameResult.getScore())
                .isEqualTo(8);

        assertThat(savedGameResult.getTotalQuestions())
                .isEqualTo(10);

        assertThat(savedGameResult.getDurationSeconds())
                .isEqualTo(120);

        assertThat(savedGameResult.getCompletedAt())
                .isNotNull();

        verify(gameAttemptRecorder).recordAttempt(
                eq(user),
                eq(flashcardSet),
                eq(request),
                any(Instant.class)
        );
    }

    @Test
    void shouldUpdateGameResultWhenResultAlreadyExists() {
        User user = createUser(2L, Role.USER);

        AuthenticatedUser authenticatedUser = createAuthenticatedUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, user);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(userRepository.getReferenceById(2L))
                .thenReturn(user);

        GameResult existingGameResult = createGameResult(
                user,
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

        assertThat(existingGameResult.getScore())
                .isEqualTo(9);

        assertThat(existingGameResult.getTotalQuestions())
                .isEqualTo(10);

        assertThat(existingGameResult.getDurationSeconds())
                .isEqualTo(120);

        assertThat(existingGameResult.getCompletedAt())
                .isNotNull();

        verify(gameResultRepository)
                .save(existingGameResult);

        verify(gameAttemptRecorder).recordAttempt(
                eq(user),
                eq(flashcardSet),
                eq(request),
                any(Instant.class)
        );
    }

    @Test
    void shouldThrowExceptionWhenScoreIsGreaterThanTotalQuestions() {
        AuthenticatedUser authenticatedUser = createAuthenticatedUser(2L, Role.USER);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        SaveGameResultRequest request = createSaveGameResultRequest(
                11L,
                GameMode.MATCHING,
                11,
                10,
                null
        );

        assertThatThrownBy(
                () -> gameResultService.saveGameResult(request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Score cannot be greater than total questions."
                );

        verifyNoInteractions(flashcardSetRepository);
        verifyNoInteractions(gameResultRepository);
        verifyNoInteractions(gameAttemptRecorder);
        verifyNoInteractions(gameResultMapper);
    }

    @Test
    void shouldThrowExceptionWhenFlashcardSetDoesNotExist() {
        AuthenticatedUser authenticatedUser = createAuthenticatedUser(2L, Role.USER);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        SaveGameResultRequest request = createSaveGameResultRequest(
                99L,
                GameMode.CUSTOM_TEST,
                5,
                10,
                null
        );

        when(flashcardSetRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> gameResultService.saveGameResult(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "FlashcardSet with id: 99 not found"
                );

        verify(gameResultRepository, never())
                .save(any());

        verifyNoInteractions(gameAttemptRecorder);
        verifyNoInteractions(gameResultMapper);
    }

    @Test
    void shouldReturnMyGameResultsWithoutSetFilter() {
        User user = createUser(2L, Role.USER);
        AuthenticatedUser authenticatedUser = createAuthenticatedUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, user);

        GameResult gameResult = createGameResult(
                user,
                flashcardSet,
                GameMode.MULTIPLE_CHOICE
        );

        GameResultDTO dto =
                createGameResultDTO(gameResult);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameResultRepository
                .findByUser_UserIdOrderByCompletedAtDesc(2L))
                .thenReturn(List.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        List<GameResultDTO> results = gameResultService.getMyGameResults(null);

        assertThat(results)
                .containsExactly(dto);

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
        User user = createUser(2L, Role.USER);
        AuthenticatedUser authenticatedUser = createAuthenticatedUser(2L, Role.USER);
        FlashcardSet flashcardSet = createFlashcardSet(11L, user);

        GameResult gameResult = createGameResult(
                user,
                flashcardSet,
                GameMode.WRITTEN
        );

        GameResultDTO dto = createGameResultDTO(gameResult);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameResultRepository
                .findByUser_UserIdAndSet_SetIdOrderByCompletedAtDesc(
                        2L,
                        11L
                ))
                .thenReturn(List.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        List<GameResultDTO> results = gameResultService.getMyGameResults(11L);

        assertThat(results)
                .containsExactly(dto);

        verify(gameResultRepository)
                .findByUser_UserIdAndSet_SetIdOrderByCompletedAtDesc(
                        2L,
                        11L
                );

        verify(gameResultRepository, never())
                .findByUser_UserIdOrderByCompletedAtDesc(
                        anyLong()
                );
    }

    @Test
    void shouldReturnAllGameResultsForAdmin() {
        AuthenticatedUser admin = createAuthenticatedUser(1L, Role.ADMIN);
        User owner = createUser(2L, Role.USER);

        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);

        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.MATCHING
        );

        GameResultDTO dto = createGameResultDTO(gameResult);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(admin);

        when(gameResultRepository
                .findAllByOrderByCompletedAtDesc())
                .thenReturn(List.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        List<GameResultDTO> results =
                gameResultService.getAllGameResults(null);

        assertThat(results)
                .containsExactly(dto);

        verify(gameResultRepository)
                .findAllByOrderByCompletedAtDesc();
    }

    @Test
    void shouldReturnAllGameResultsForSetForAdmin() {
        AuthenticatedUser admin = createAuthenticatedUser(1L, Role.ADMIN);
        User owner = createUser(2L, Role.USER);

        FlashcardSet flashcardSet =
                createFlashcardSet(11L, owner);

        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.CUSTOM_TEST
        );

        GameResultDTO dto =
                createGameResultDTO(gameResult);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(admin);

        when(gameResultRepository
                .findBySet_SetIdOrderByCompletedAtDesc(11L))
                .thenReturn(List.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        List<GameResultDTO> results =
                gameResultService.getAllGameResults(11L);

        assertThat(results)
                .containsExactly(dto);

        verify(gameResultRepository)
                .findBySet_SetIdOrderByCompletedAtDesc(11L);
    }

    @Test
    void shouldThrowAccessDeniedWhenNonAdminGetsAllGameResults() {
        AuthenticatedUser user = createAuthenticatedUser(2L, Role.USER);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(user);

        assertThatThrownBy(
                () -> gameResultService.getAllGameResults(null)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(
                        "Only admins can access this resource."
                );

        verifyNoInteractions(gameResultRepository);
        verifyNoInteractions(gameResultMapper);
    }

    @Test
    void shouldReturnGameResultByIdForOwner() {
        User owner = createUser(2L, Role.USER);
        AuthenticatedUser authenticatedOwner = createAuthenticatedUser(2L, Role.USER);
        FlashcardSet flashcardSet =
                createFlashcardSet(11L, owner);

        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.CUSTOM_TEST
        );

        GameResultDTO dto = createGameResultDTO(gameResult);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedOwner);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        GameResultDTO result = gameResultService.getGameResultById(1L);

        assertThat(result)
                .isEqualTo(dto);
    }

    @Test
    void shouldReturnGameResultByIdForAdmin() {
        AuthenticatedUser admin = createAuthenticatedUser(1L, Role.ADMIN);
        User owner = createUser(2L, Role.USER);

        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);

        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.CUSTOM_TEST
        );

        GameResultDTO dto = createGameResultDTO(gameResult);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(admin);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        when(gameResultMapper.toDto(gameResult))
                .thenReturn(dto);

        GameResultDTO result =
                gameResultService.getGameResultById(1L);

        assertThat(result)
                .isEqualTo(dto);
    }

    @Test
    void shouldThrowExceptionWhenGameResultByIdDoesNotExist() {
        AuthenticatedUser authenticatedUser = createAuthenticatedUser(2L, Role.USER);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameResultRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> gameResultService.getGameResultById(99L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "GameResult with id: 99 not found"
                );

        verify(gameResultMapper, never())
                .toDto(any());
    }

    @Test
    void shouldThrowAccessDeniedWhenUserGetsSomeoneElseGameResult() {
        AuthenticatedUser authenticatedUser = createAuthenticatedUser(3L, Role.USER);
        User owner = createUser(2L, Role.USER);

        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);

        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.WRITTEN
        );

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        assertThatThrownBy(
                () -> gameResultService.getGameResultById(1L)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(
                        "You are not allowed to access this game result."
                );

        verify(gameResultMapper, never())
                .toDto(any());
    }

    @Test
    void shouldDeleteGameResultForOwner() {
        User owner = createUser(2L, Role.USER);

        AuthenticatedUser authenticatedOwner = createAuthenticatedUser(2L, Role.USER);

        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);

        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.MATCHING
        );

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedOwner);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        gameResultService.deleteGameResult(1L);

        verify(gameResultRepository)
                .delete(gameResult);
    }

    @Test
    void shouldDeleteGameResultForAdmin() {
        AuthenticatedUser admin = createAuthenticatedUser(1L, Role.ADMIN);
        User owner = createUser(2L, Role.USER);

        FlashcardSet flashcardSet =
                createFlashcardSet(11L, owner);

        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.MATCHING
        );

        when(currentUserProvider.getCurrentUser())
                .thenReturn(admin);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        gameResultService.deleteGameResult(1L);

        verify(gameResultRepository)
                .delete(gameResult);
    }

    @Test
    void shouldThrowAccessDeniedWhenUserDeletesSomeoneElseGameResult() {
        AuthenticatedUser authenticatedUser = createAuthenticatedUser(3L, Role.USER);
        User owner = createUser(2L, Role.USER);

        FlashcardSet flashcardSet = createFlashcardSet(11L, owner);

        GameResult gameResult = createGameResult(
                owner,
                flashcardSet,
                GameMode.MATCHING
        );

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameResultRepository.findById(1L))
                .thenReturn(Optional.of(gameResult));

        assertThatThrownBy(
                () -> gameResultService.deleteGameResult(1L)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(
                        "You are not allowed to delete this game result."
                );

        verify(gameResultRepository, never())
                .delete(any());
    }
}