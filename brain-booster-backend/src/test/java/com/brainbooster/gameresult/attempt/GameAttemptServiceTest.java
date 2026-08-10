package com.brainbooster.gameresult.attempt;

import com.brainbooster.gameresult.dto.GameAttemptDTO;
import com.brainbooster.gameresult.dto.GameAttemptSummaryDTO;
import com.brainbooster.gameresult.dto.GameQuestionResultDTO;
import com.brainbooster.gameresult.mapper.GameAttemptMapper;
import com.brainbooster.gameresult.mapper.GameQuestionResultMapper;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import com.brainbooster.gameresult.questionresult.GameQuestionResultRepository;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.security.authorization.OwnerOrAdminPolicy;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameAttemptServiceTest {

    private static final String ACCESS_DENIED_MESSAGE =
            "You do not have permission to access this game attempt.";

    @Mock
    private GameAttemptRepository gameAttemptRepository;

    @Mock
    private GameAttemptMapper gameAttemptMapper;

    @Mock
    private GameQuestionResultRepository gameQuestionResultRepository;

    @Mock
    private GameQuestionResultMapper gameQuestionResultMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private final OwnerOrAdminPolicy ownerOrAdminPolicy = new OwnerOrAdminPolicy();

    private GameAttemptService gameAttemptService;

    @BeforeEach
    void setUp() {
        gameAttemptService = new GameAttemptService(
                gameAttemptRepository,
                gameAttemptMapper,
                gameQuestionResultRepository,
                gameQuestionResultMapper,
                currentUserProvider,
                ownerOrAdminPolicy);
    }

    @Test
    void getMyGameAttempts_ShouldReturnAuthenticatedUserAttemptsWithFilters() {
        // given
        User authenticatedUser = TestEntities.createUser();

        Long setId = 1L;
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 10);

        Instant expectedFrom = Instant.parse("2026-08-01T00:00:00Z");

        Instant expectedToExclusive = Instant.parse("2026-08-11T00:00:00Z");

        Pageable pageable = PageRequest.of(0, 20);

        GameAttempt gameAttempt = mock(GameAttempt.class);
        GameAttemptSummaryDTO summaryDTO = mock(GameAttemptSummaryDTO.class);

        Page<GameAttempt> attempts = new PageImpl<>(
                List.of(gameAttempt),
                pageable,
                1
        );

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttemptRepository.findByUserIdWithFilters(
                authenticatedUser.getUserId(),
                setId,
                null,
                expectedFrom,
                expectedToExclusive,
                pageable
        )).thenReturn(attempts);

        when(gameAttemptMapper.toSummaryDto(gameAttempt))
                .thenReturn(summaryDTO);

        // when
        Page<GameAttemptSummaryDTO> result = gameAttemptService.getMyGameAttempts(
                        setId,
                        null,
                        from,
                        to,
                        pageable);

        // then
        assertThat(result.getContent())
                .containsExactly(summaryDTO);

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        verify(gameAttemptRepository).findByUserIdWithFilters(
                authenticatedUser.getUserId(),
                setId,
                null,
                expectedFrom,
                expectedToExclusive,
                pageable
        );

        verify(gameAttemptMapper).toSummaryDto(gameAttempt);
    }

    @Test
    void getMyGameAttemptsBySetId_ShouldReturnAuthenticatedUserAttempts() {
        // given
        User authenticatedUser = TestEntities.createUser();
        Long setId = 1L;
        Pageable pageable = PageRequest.of(0, 20);

        GameAttempt gameAttempt = mock(GameAttempt.class);
        GameAttemptSummaryDTO summaryDTO = mock(GameAttemptSummaryDTO.class);

        Page<GameAttempt> attempts =
                new PageImpl<>(
                        List.of(gameAttempt),
                        pageable,
                        1
                );

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttemptRepository.findByUserIdWithFilters(
                authenticatedUser.getUserId(),
                setId,
                null,
                null,
                null,
                pageable
        )).thenReturn(attempts);

        when(gameAttemptMapper.toSummaryDto(gameAttempt))
                .thenReturn(summaryDTO);

        // when
        Page<GameAttemptSummaryDTO> result = gameAttemptService.getMyGameAttemptsBySetId(
                        setId,
                        null,
                        null,
                        null,
                        pageable
                );

        // then
        assertThat(result.getContent())
                .containsExactly(summaryDTO);

        verify(gameAttemptRepository).findByUserIdWithFilters(
                authenticatedUser.getUserId(),
                setId,
                null,
                null,
                null,
                pageable
        );
    }

    @Test
    void getMyGameAttempts_ShouldThrowBadRequest_WhenModeIsInvalid() {
        // given
        User authenticatedUser = TestEntities.createUser();
        Pageable pageable = PageRequest.of(0, 20);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        // when + then
        assertThatThrownBy(() ->
                gameAttemptService.getMyGameAttempts(
                        1L,
                        "invalid-mode",
                        null,
                        null,
                        pageable
                )
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseException =
                            (ResponseStatusException) exception;

                    assertThat(responseException.getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST);

                    assertThat(responseException.getReason())
                            .isEqualTo("Invalid game mode: invalid-mode");
                });

        verify(gameAttemptRepository, never())
                .findByUserIdWithFilters(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void getGameAttemptById_ShouldReturnGameAttempt_WhenUserIsOwner() {
        // given
        User authenticatedUser = TestEntities.createUser();

        GameAttempt gameAttempt = mock(GameAttempt.class);
        GameAttemptDTO expectedDTO = mock(GameAttemptDTO.class);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttempt.getUser())
                .thenReturn(authenticatedUser);

        when(gameAttemptRepository.findWithQuestionResultsByAttemptId(1L))
                .thenReturn(Optional.of(gameAttempt));

        when(gameAttemptMapper.toDto(gameAttempt))
                .thenReturn(expectedDTO);

        // when
        GameAttemptDTO result = gameAttemptService.getGameAttemptById(1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);

        verify(gameAttemptMapper).toDto(gameAttempt);
    }

    @Test
    void getGameAttemptById_ShouldReturnGameAttempt_WhenUserIsAdmin() {
        // given
        User owner = TestEntities.createUser();

        User admin = TestEntities.userBuilder()
                .userId(2L)
                .role(Role.ADMIN)
                .build();

        GameAttempt gameAttempt = mock(GameAttempt.class);
        GameAttemptDTO expectedDTO = mock(GameAttemptDTO.class);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(admin);

        when(gameAttempt.getUser())
                .thenReturn(owner);

        when(gameAttemptRepository.findWithQuestionResultsByAttemptId(1L))
                .thenReturn(Optional.of(gameAttempt));

        when(gameAttemptMapper.toDto(gameAttempt))
                .thenReturn(expectedDTO);

        // when
        GameAttemptDTO result = gameAttemptService.getGameAttemptById(1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);

        verify(gameAttemptMapper).toDto(gameAttempt);
    }

    @Test
    void getGameAttemptById_ShouldThrowAccessDenied_WhenUserIsNotOwnerOrAdmin() {
        // given
        User owner = TestEntities.createUser(1L, Role.USER);
        User authenticatedUser =
                TestEntities.createUser(2L, Role.USER);

        GameAttempt gameAttempt = mock(GameAttempt.class);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttempt.getUser())
                .thenReturn(owner);

        when(gameAttemptRepository.findWithQuestionResultsByAttemptId(1L))
                .thenReturn(Optional.of(gameAttempt));

        // when + then
        assertThatThrownBy(() ->
                gameAttemptService.getGameAttemptById(1L)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(ACCESS_DENIED_MESSAGE);

        verify(gameAttemptMapper, never())
                .toDto(any(GameAttempt.class));
    }

    @Test
    void getGameAttemptById_ShouldThrowNotFound_WhenGameAttemptDoesNotExist() {
        // given
        User authenticatedUser = TestEntities.createUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttemptRepository.findWithQuestionResultsByAttemptId(999L))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() ->
                gameAttemptService.getGameAttemptById(999L)
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseException =
                            (ResponseStatusException) exception;

                    assertThat(responseException.getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND);

                    assertThat(responseException.getReason())
                            .isEqualTo("Game attempt not found");
                });

        verify(gameAttemptMapper, never())
                .toDto(any(GameAttempt.class));
    }

    @Test
    void getQuestionResultsByAttemptId_ShouldReturnMappedResults_WhenUserIsOwner() {
        // given
        User authenticatedUser = TestEntities.createUser();

        GameAttempt gameAttempt = mock(GameAttempt.class);

        GameQuestionResult firstQuestionResult = mock(GameQuestionResult.class);

        GameQuestionResult secondQuestionResult = mock(GameQuestionResult.class);

        GameQuestionResultDTO firstDTO = mock(GameQuestionResultDTO.class);

        GameQuestionResultDTO secondDTO = mock(GameQuestionResultDTO.class);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttempt.getUser())
                .thenReturn(authenticatedUser);

        when(gameAttemptRepository.findById(1L))
                .thenReturn(Optional.of(gameAttempt));

        when(gameQuestionResultRepository
                .findByAttempt_AttemptIdOrderByQuestionOrderAsc(1L))
                .thenReturn(List.of(
                        firstQuestionResult,
                        secondQuestionResult
                ));

        when(gameQuestionResultMapper.toDto(firstQuestionResult))
                .thenReturn(firstDTO);

        when(gameQuestionResultMapper.toDto(secondQuestionResult))
                .thenReturn(secondDTO);

        // when
        List<GameQuestionResultDTO> result = gameAttemptService.getQuestionResultsByAttemptId(1L);

        // then
        assertThat(result)
                .containsExactly(firstDTO, secondDTO);

        verify(gameQuestionResultRepository)
                .findByAttempt_AttemptIdOrderByQuestionOrderAsc(1L);

        verify(gameQuestionResultMapper)
                .toDto(firstQuestionResult);

        verify(gameQuestionResultMapper)
                .toDto(secondQuestionResult);
    }

    @Test
    void getQuestionResultsByAttemptId_ShouldThrowAccessDenied_WhenUserIsNotOwnerOrAdmin() {
        // given
        User owner = TestEntities.createUser(1L, Role.USER);
        User authenticatedUser = TestEntities.createUser(2L, Role.USER);

        GameAttempt gameAttempt = mock(GameAttempt.class);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttempt.getUser())
                .thenReturn(owner);

        when(gameAttemptRepository.findById(1L))
                .thenReturn(Optional.of(gameAttempt));

        // when, then
        assertThatThrownBy(() -> gameAttemptService.getQuestionResultsByAttemptId(1L)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(ACCESS_DENIED_MESSAGE);

        verify(gameQuestionResultRepository, never())
                .findByAttempt_AttemptIdOrderByQuestionOrderAsc(anyLong());
    }

    @Test
    void getQuestionResultsByAttemptId_ShouldThrowNotFound_WhenGameAttemptDoesNotExist() {
        // given
        User authenticatedUser = TestEntities.createUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttemptRepository.findById(999L))
                .thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> gameAttemptService.getQuestionResultsByAttemptId(999L)
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseException =
                            (ResponseStatusException) exception;

                    assertThat(responseException.getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND);

                    assertThat(responseException.getReason())
                            .isEqualTo("Game attempt not found");
                });

        verify(gameQuestionResultRepository, never())
                .findByAttempt_AttemptIdOrderByQuestionOrderAsc(anyLong());
    }
}
