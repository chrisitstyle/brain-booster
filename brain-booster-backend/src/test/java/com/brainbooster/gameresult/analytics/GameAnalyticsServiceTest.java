package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.WeakFlashcardDTO;
import com.brainbooster.gameresult.analytics.dto.GameAnalyticsSummaryDTO;
import com.brainbooster.gameresult.analytics.dto.GameProgressPointDTO;
import com.brainbooster.gameresult.analytics.dto.QuestionTypeAnalyticsDTO;
import com.brainbooster.gameresult.attempt.GameAttempt;
import com.brainbooster.gameresult.attempt.GameAttemptRepository;
import com.brainbooster.gameresult.attempt.GameAttemptSummaryProjection;
import com.brainbooster.gameresult.questionresult.GameQuestionResultRepository;
import com.brainbooster.gameresult.questionresult.QuestionTypeStatisticsProjection;
import com.brainbooster.gameresult.questionresult.WeakFlashcardStatisticsProjection;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
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
    @Mock
    private WeakFlashcardAnalyzer weakFlashcardAnalyzer;
    @Mock
    private QuestionTypeAnalyzer questionTypeAnalyzer;

    @InjectMocks
    private GameAnalyticsService gameAnalyticsService;


    @Test
    void getMySetSummary_shouldReturnEmptySummary_whenUserHasNoAttempts() {
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();

        Long flashcardSetId = 1L;

        GameAttemptSummaryProjection attemptSummary = mock(GameAttemptSummaryProjection.class);

        stubAttemptSummaryForAuthenticatedUser(
                authenticatedUser,
                flashcardSetId,
                attemptSummary);

        when(attemptSummary.getTotalAttempts()).thenReturn(0L);

        GameAnalyticsSummaryDTO actualSummary = gameAnalyticsService.getMySetSummary(flashcardSetId);

        GameAnalyticsSummaryDTO expectedSummary = new GameAnalyticsSummaryDTO(
                0L,
                0.0,
                0,
                0.0,
                null,
                0.0);

        assertThat(actualSummary).isEqualTo(expectedSummary);

        verify(gameAttemptRepository).findSummaryByUserIdAndSetId(
                authenticatedUser.userId(),
                flashcardSetId);
    }

    @Test
    void getMySetSummary_shouldCalculateSummary_whenAttemptsExist() {
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();

        Long flashcardSetId = 1L;

        Instant lastCompletedAt = Instant.parse("2026-08-02T10:00:00Z");

        GameAttemptSummaryProjection attemptSummary = mock(GameAttemptSummaryProjection.class);

        stubAttemptSummaryForAuthenticatedUser(
                authenticatedUser,
                flashcardSetId,
                attemptSummary);

        when(attemptSummary.getTotalAttempts()).thenReturn(2L);
        when(attemptSummary.getTotalScore()).thenReturn(16L);
        when(attemptSummary.getTotalQuestions()).thenReturn(20L);
        when(attemptSummary.getAverageScore()).thenReturn(8.0);
        when(attemptSummary.getBestScore()).thenReturn(9);
        when(attemptSummary.getAverageDurationSeconds())
                .thenReturn(90.0);
        when(attemptSummary.getLastCompletedAt())
                .thenReturn(lastCompletedAt);

        GameAnalyticsSummaryDTO actualSummary = gameAnalyticsService.getMySetSummary(flashcardSetId);

        GameAnalyticsSummaryDTO expectedSummary = new GameAnalyticsSummaryDTO(
                2L,
                8.0,
                9,
                90.0,
                lastCompletedAt,
                80.0);

        assertThat(actualSummary).isEqualTo(expectedSummary);
    }

    @Test
    void getMySetSummary_shouldCalculateWeightedAccuracy_whenQuestionCountsDiffer() {
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();

        Long flashcardSetId = 1L;

        GameAttemptSummaryProjection attemptSummary =
                mock(GameAttemptSummaryProjection.class);

        stubAttemptSummaryForAuthenticatedUser(
                authenticatedUser,
                flashcardSetId,
                attemptSummary);

        // First attempt - 1/2 correct answers
        // Second attempt - 9/10 correct answers
        // Weighted accuracy - 10/12 = 83.33%

        when(attemptSummary.getTotalAttempts()).thenReturn(2L);
        when(attemptSummary.getTotalScore()).thenReturn(10L);
        when(attemptSummary.getTotalQuestions()).thenReturn(12L);
        when(attemptSummary.getAverageScore()).thenReturn(5.0);
        when(attemptSummary.getBestScore()).thenReturn(9);
        when(attemptSummary.getAverageDurationSeconds())
                .thenReturn(80.0);

        GameAnalyticsSummaryDTO actualSummary =
                gameAnalyticsService.getMySetSummary(flashcardSetId);

        assertThat(actualSummary.totalAttempts()).isEqualTo(2L);
        assertThat(actualSummary.averageScore()).isEqualTo(5.0);
        assertThat(actualSummary.bestScore()).isEqualTo(9);
        assertThat(actualSummary.accuracyPercentage())
                .isEqualTo(83.33);
    }

    @Test
    void getMySetSummary_shouldReturnZeroAverageDuration_whenAllDurationsAreNull() {
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();

        Long flashcardSetId = 1L;

        GameAttemptSummaryProjection attemptSummary = mock(GameAttemptSummaryProjection.class);

        stubAttemptSummaryForAuthenticatedUser(
                authenticatedUser,
                flashcardSetId,
                attemptSummary);

        when(attemptSummary.getTotalAttempts()).thenReturn(1L);
        when(attemptSummary.getTotalScore()).thenReturn(2L);
        when(attemptSummary.getTotalQuestions()).thenReturn(4L);
        when(attemptSummary.getAverageScore()).thenReturn(2.0);
        when(attemptSummary.getBestScore()).thenReturn(2);

        // Mockito returns null for an unstubbed Double getter

        GameAnalyticsSummaryDTO actualSummary =
                gameAnalyticsService.getMySetSummary(flashcardSetId);

        assertThat(actualSummary.averageDuration()).isZero();
        assertThat(actualSummary.accuracyPercentage())
                .isEqualTo(50.0);
    }

    private void stubAttemptSummaryForAuthenticatedUser(
            AuthenticatedUser authenticatedUser,
            Long flashcardSetId,
            GameAttemptSummaryProjection attemptSummary
    ) {
        when(currentUserProvider.getCurrentUser()).thenReturn(authenticatedUser);

        when(gameAttemptRepository.findSummaryByUserIdAndSetId(
                authenticatedUser.userId(),
                flashcardSetId
        )).thenReturn(attemptSummary);
    }


    @Test
    void getMySetProgress_ShouldReturnProgressForAuthenticatedUser() {
        // given
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();
        Long setId = 1L;

        Instant completedAt = Instant.parse("2026-08-01T10:00:00Z");

        GameAttempt attempt = mock(GameAttempt.class);

        when(attempt.getAttemptId()).thenReturn(5L);
        when(attempt.getCompletedAt()).thenReturn(completedAt);
        when(attempt.getScore()).thenReturn(8);
        when(attempt.getTotalQuestions()).thenReturn(10);
        when(attempt.getDurationSeconds()).thenReturn(75);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameAttemptRepository.findByUserIdAndSetIdOrderByCompletedAtAsc(
                authenticatedUser.userId(),
                setId
        )).thenReturn(List.of(attempt));

        // when
        List<GameProgressPointDTO> result = gameAnalyticsService.getMySetProgress(setId);

        // then
        GameProgressPointDTO expected = new GameProgressPointDTO(
                5L,
                completedAt,
                8,
                10,
                80.0,
                75,
                null);

        assertThat(result)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(expected);

        verify(gameAttemptRepository)
                .findByUserIdAndSetIdOrderByCompletedAtAsc(
                        authenticatedUser.userId(),
                        setId);
    }

    @Test
    void getMySetWeakFlashcards_shouldDelegateAggregatedStatistics_whenUserIsAuthenticated() {
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();

        Long flashcardSetId = 1L;

        WeakFlashcardStatisticsProjection flashcardStatistics = mock(WeakFlashcardStatisticsProjection.class);

        List<WeakFlashcardStatisticsProjection> aggregatedStatistics = List.of(flashcardStatistics);

        WeakFlashcardDTO expectedFlashcard = new WeakFlashcardDTO(
                1L,
                "Weak term",
                "Weak definition",
                2L,
                1L,
                1L,
                2,
                50.0,
                Instant.parse("2026-08-02T10:00:00Z"));

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameQuestionResultRepository.findFlashcardStatisticsByUserIdAndSetId(
                authenticatedUser.userId(),
                flashcardSetId
        )).thenReturn(aggregatedStatistics);

        when(weakFlashcardAnalyzer.analyzeAggregates(aggregatedStatistics))
                .thenReturn(List.of(expectedFlashcard));

        List<WeakFlashcardDTO> actualFlashcards = gameAnalyticsService
                .getMySetWeakFlashcards(flashcardSetId);

        assertThat(actualFlashcards).containsExactly(expectedFlashcard);

        verify(gameQuestionResultRepository)
                .findFlashcardStatisticsByUserIdAndSetId(
                        authenticatedUser.userId(),
                        flashcardSetId);

        verify(weakFlashcardAnalyzer).analyzeAggregates(aggregatedStatistics);
    }

    @Test
    void getMySetQuestionTypeAnalytics_shouldDelegateAggregatedStatistics_whenUserIsAuthenticated() {
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();

        Long flashcardSetId = 1L;

        QuestionTypeStatisticsProjection questionTypeStatistics = mock(QuestionTypeStatisticsProjection.class);

        List<QuestionTypeStatisticsProjection> aggregatedStatistics = List.of(questionTypeStatistics);

        QuestionTypeAnalyticsDTO expectedStatistics = new QuestionTypeAnalyticsDTO(
                GameQuestionType.MULTIPLE_CHOICE,
                3L,
                2L,
                1L,
                66.67);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(gameQuestionResultRepository.findQuestionTypeStatisticsByUserIdAndSetId(
                authenticatedUser.userId(),
                flashcardSetId
        )).thenReturn(aggregatedStatistics);

        when(questionTypeAnalyzer.analyzeAggregates(aggregatedStatistics))
                .thenReturn(List.of(expectedStatistics));

        List<QuestionTypeAnalyticsDTO> actualStatistics = gameAnalyticsService
                .getMySetQuestionTypeAnalytics(flashcardSetId);

        assertThat(actualStatistics).containsExactly(expectedStatistics);

        verify(gameQuestionResultRepository)
                .findQuestionTypeStatisticsByUserIdAndSetId(
                        authenticatedUser.userId(),
                        flashcardSetId);

        verify(questionTypeAnalyzer).analyzeAggregates(aggregatedStatistics);
    }

}