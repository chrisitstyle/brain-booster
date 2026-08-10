package com.brainbooster.gameresult.analytics;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.WeakFlashcardDTO;
import com.brainbooster.gameresult.analytics.dto.GameAnalyticsSummaryDTO;
import com.brainbooster.gameresult.analytics.dto.GameProgressPointDTO;
import com.brainbooster.gameresult.analytics.dto.QuestionTypeAnalyticsDTO;
import com.brainbooster.gameresult.attempt.GameAttempt;
import com.brainbooster.gameresult.attempt.GameAttemptRepository;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import com.brainbooster.gameresult.questionresult.GameQuestionResultRepository;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.user.User;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameAnalyticsServiceTest {

    @Mock
    private GameAttemptRepository gameAttemptRepository;
    @Mock
    private GameQuestionResultRepository gameQuestionResultRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private GameAnalyticsService gameAnalyticsService;

    @Test
    void getMySetSummary_ShouldReturnEmptySummary_WhenUserHasNoAttempts() {
        // given
        User authenticatedUser = TestEntities.createUser();
        Long setId = 1L;

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttemptRepository.findByUserIdAndSetIdOrderByCompletedAtAsc(
                authenticatedUser.getUserId(),
                setId
        )).thenReturn(List.of());

        // when
        GameAnalyticsSummaryDTO result =
                gameAnalyticsService.getMySetSummary(setId);

        // then
        GameAnalyticsSummaryDTO expected = new GameAnalyticsSummaryDTO(
                0L,
                0.0,
                0,
                0.0,
                null,
                0.0
        );

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        verify(gameAttemptRepository)
                .findByUserIdAndSetIdOrderByCompletedAtAsc(
                        authenticatedUser.getUserId(),
                        setId
                );
    }

    @Test
    void getMySetSummary_ShouldCalculateSummary_WhenAttemptsExist() {
        // given
        User authenticatedUser = TestEntities.createUser();
        Long setId = 1L;

        Instant firstCompletedAt = Instant.parse("2026-08-01T10:00:00Z");

        Instant secondCompletedAt = Instant.parse("2026-08-02T10:00:00Z");

        GameAttempt firstAttempt = mock(GameAttempt.class);
        GameAttempt secondAttempt = mock(GameAttempt.class);

        when(firstAttempt.getScore()).thenReturn(7);
        when(firstAttempt.getTotalQuestions()).thenReturn(10);
        when(firstAttempt.getDurationSeconds()).thenReturn(60);
        when(firstAttempt.getCompletedAt()).thenReturn(firstCompletedAt);

        when(secondAttempt.getScore()).thenReturn(9);
        when(secondAttempt.getTotalQuestions()).thenReturn(10);
        when(secondAttempt.getDurationSeconds()).thenReturn(120);
        when(secondAttempt.getCompletedAt()).thenReturn(secondCompletedAt);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttemptRepository.findByUserIdAndSetIdOrderByCompletedAtAsc(
                authenticatedUser.getUserId(),
                setId
        )).thenReturn(List.of(firstAttempt, secondAttempt));

        // when
        GameAnalyticsSummaryDTO result = gameAnalyticsService.getMySetSummary(setId);

        // then
        GameAnalyticsSummaryDTO expected = new GameAnalyticsSummaryDTO(
                2L,
                8.0,
                9,
                90.0,
                secondCompletedAt,
                80.0
        );

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void getMySetProgress_ShouldReturnProgressForAuthenticatedUser() {
        // given
        User authenticatedUser = TestEntities.createUser();
        Long setId = 1L;

        Instant completedAt =
                Instant.parse("2026-08-01T10:00:00Z");

        GameAttempt attempt = mock(GameAttempt.class);

        when(attempt.getAttemptId()).thenReturn(5L);
        when(attempt.getCompletedAt()).thenReturn(completedAt);
        when(attempt.getScore()).thenReturn(8);
        when(attempt.getTotalQuestions()).thenReturn(10);
        when(attempt.getDurationSeconds()).thenReturn(75);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttemptRepository.findByUserIdAndSetIdOrderByCompletedAtAsc(
                authenticatedUser.getUserId(),
                setId
        )).thenReturn(List.of(attempt));

        // when
        List<GameProgressPointDTO> result =
                gameAnalyticsService.getMySetProgress(setId);

        // then
        GameProgressPointDTO expected = new GameProgressPointDTO(
                5L,
                completedAt,
                8,
                10,
                80.0,
                75,
                null
        );

        assertThat(result)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(expected);

        verify(gameAttemptRepository)
                .findByUserIdAndSetIdOrderByCompletedAtAsc(
                        authenticatedUser.getUserId(),
                        setId
                );
    }

    @Test
    void getMySetWeakFlashcards_ShouldReturnWeakFlashcards() {
        // given
        User authenticatedUser = TestEntities.createUser();
        Long setId = 1L;

        Instant firstAnsweredAt = Instant.parse("2026-08-01T10:00:00Z");

        Instant secondAnsweredAt = Instant.parse("2026-08-02T10:00:00Z");

        Instant thirdAnsweredAt = Instant.parse("2026-08-03T10:00:00Z");

        Flashcard weakFlashcard = mock(Flashcard.class);
        Flashcard strongFlashcard = mock(Flashcard.class);

        when(weakFlashcard.getFlashcardId()).thenReturn(1L);
        when(weakFlashcard.getTerm()).thenReturn("Weak term");
        when(weakFlashcard.getDefinition()).thenReturn("Weak definition");

        when(strongFlashcard.getFlashcardId()).thenReturn(2L);

        GameQuestionResult incorrectResult = mock(GameQuestionResult.class);

        GameQuestionResult correctResult = mock(GameQuestionResult.class);

        GameQuestionResult strongCorrectResult = mock(GameQuestionResult.class);

        when(incorrectResult.getFlashcard())
                .thenReturn(weakFlashcard);
        when(incorrectResult.getWasCorrect())
                .thenReturn(false);
        when(incorrectResult.getMistakesCount())
                .thenReturn(2);
        when(incorrectResult.getAnsweredAt())
                .thenReturn(secondAnsweredAt);

        when(correctResult.getFlashcard())
                .thenReturn(weakFlashcard);
        when(correctResult.getWasCorrect())
                .thenReturn(true);
        when(correctResult.getMistakesCount())
                .thenReturn(0);
        when(correctResult.getAnsweredAt())
                .thenReturn(firstAnsweredAt);

        when(strongCorrectResult.getFlashcard())
                .thenReturn(strongFlashcard);
        when(strongCorrectResult.getWasCorrect())
                .thenReturn(true);
        when(strongCorrectResult.getMistakesCount())
                .thenReturn(0);
        when(strongCorrectResult.getAnsweredAt())
                .thenReturn(thirdAnsweredAt);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameQuestionResultRepository
                .findByUserIdAndSetIdOrderByAnsweredAtDesc(
                        authenticatedUser.getUserId(),
                        setId
                ))
                .thenReturn(List.of(
                        strongCorrectResult,
                        incorrectResult,
                        correctResult));

        // when
        List<WeakFlashcardDTO> result = gameAnalyticsService.getMySetWeakFlashcards(setId);

        // then
        WeakFlashcardDTO expected = new WeakFlashcardDTO(
                1L,
                "Weak term",
                "Weak definition",
                2L,
                1L,
                1L,
                2,
                50.0,
                secondAnsweredAt
        );

        assertThat(result)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(expected);

        verify(gameQuestionResultRepository)
                .findByUserIdAndSetIdOrderByAnsweredAtDesc(
                        authenticatedUser.getUserId(),
                        setId);
    }

    @Test
    void getMySetQuestionTypeAnalytics_ShouldReturnAggregatedAnalytics() {
        // given
        User authenticatedUser = TestEntities.createUser();
        Long setId = 1L;

        GameQuestionType questionType = GameQuestionType.values()[0];
        GameQuestionResult firstCorrectResult = mock(GameQuestionResult.class);
        GameQuestionResult secondCorrectResult = mock(GameQuestionResult.class);
        GameQuestionResult incorrectResult = mock(GameQuestionResult.class);

        when(firstCorrectResult.getQuestionType())
                .thenReturn(questionType);
        when(firstCorrectResult.getWasCorrect())
                .thenReturn(true);

        when(secondCorrectResult.getQuestionType())
                .thenReturn(questionType);
        when(secondCorrectResult.getWasCorrect())
                .thenReturn(true);

        when(incorrectResult.getQuestionType())
                .thenReturn(questionType);
        when(incorrectResult.getWasCorrect())
                .thenReturn(false);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameQuestionResultRepository
                .findByUserIdAndSetIdOrderByAnsweredAtDesc(
                        authenticatedUser.getUserId(),
                        setId
                ))
                .thenReturn(List.of(
                        firstCorrectResult,
                        secondCorrectResult,
                        incorrectResult
                ));

        // when
        List<QuestionTypeAnalyticsDTO> result =
                gameAnalyticsService.getMySetQuestionTypeAnalytics(setId);

        // then
        QuestionTypeAnalyticsDTO expected =
                new QuestionTypeAnalyticsDTO(
                        questionType,
                        3L,
                        2L,
                        1L,
                        66.67
                );

        assertThat(result)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(expected);

        verify(gameQuestionResultRepository)
                .findByUserIdAndSetIdOrderByAnsweredAtDesc(
                        authenticatedUser.getUserId(),
                        setId
                );
    }
}
