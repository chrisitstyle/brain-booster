package com.brainbooster.gameresult.analytics;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameAnalyticsService {

    private final GameAttemptRepository gameAttemptRepository;
    private final GameQuestionResultRepository gameQuestionResultRepository;
    private final CurrentUserProvider currentUserProvider;
    private final WeakFlashcardAnalyzer weakFlashcardAnalyzer;
    private final QuestionTypeAnalyzer questionTypeAnalyzer;


    @Transactional(readOnly = true)
    public GameAnalyticsSummaryDTO getMySetSummary(Long setId) {
        AuthenticatedUser currentUser =
                currentUserProvider.getCurrentUser();

        GameAttemptSummaryProjection attemptSummary = gameAttemptRepository
                .findSummaryByUserIdAndSetId(
                        currentUser.userId(),
                        setId);

        long totalAttempts = attemptSummary.getTotalAttempts();

        if (totalAttempts == 0) {
            return new GameAnalyticsSummaryDTO(
                    0L,
                    0.0,
                    0,
                    0.0,
                    null,
                    0.0);
        }

        Double averageDurationSeconds = attemptSummary.getAverageDurationSeconds();

        double averageDuration = averageDurationSeconds == null
                ? 0.0
                : averageDurationSeconds;

        return new GameAnalyticsSummaryDTO(
                totalAttempts,
                roundToTwoDecimals(attemptSummary.getAverageScore()),
                attemptSummary.getBestScore(),
                roundToTwoDecimals(averageDuration),
                attemptSummary.getLastCompletedAt(),
                calculateAccuracyPercentage(
                        attemptSummary.getTotalScore(),
                        attemptSummary.getTotalQuestions()));
    }


    @Transactional(readOnly = true)
    public List<GameProgressPointDTO> getMySetProgress(Long setId) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        return gameAttemptRepository
                .findByUserIdAndSetIdOrderByCompletedAtAsc(
                        currentUser.userId(),
                        setId
                )
                .stream()
                .map(this::mapToProgressPoint)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<WeakFlashcardDTO> getMySetWeakFlashcards(Long setId) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        List<WeakFlashcardStatisticsProjection> flashcardStatistics = gameQuestionResultRepository
                .findFlashcardStatisticsByUserIdAndSetId(
                        currentUser.userId(),
                        setId);

        return weakFlashcardAnalyzer.analyzeAggregates(flashcardStatistics);
    }

    @Transactional(readOnly = true)
    public List<QuestionTypeAnalyticsDTO> getMySetQuestionTypeAnalytics(Long setId) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        List<QuestionTypeStatisticsProjection> questionTypeStatistics = gameQuestionResultRepository
                .findQuestionTypeStatisticsByUserIdAndSetId(
                        currentUser.userId(),
                        setId);

        return questionTypeAnalyzer.analyzeAggregates(questionTypeStatistics);
    }


    private GameProgressPointDTO mapToProgressPoint(GameAttempt attempt) {
        return new GameProgressPointDTO(
                attempt.getAttemptId(),
                attempt.getCompletedAt(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                calculateAccuracyPercentage(
                        attempt.getScore(),
                        attempt.getTotalQuestions()
                ),
                attempt.getDurationSeconds(),
                attempt.getMode());
    }


    private static double calculateAccuracyPercentage(
            long correctAnswers,
            long totalQuestions
    ) {
        if (totalQuestions == 0) {
            return 0.0;
        }

        double accuracyPercentage = ((double) correctAnswers / totalQuestions) * 100;

        return roundToTwoDecimals(accuracyPercentage);
    }

    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}