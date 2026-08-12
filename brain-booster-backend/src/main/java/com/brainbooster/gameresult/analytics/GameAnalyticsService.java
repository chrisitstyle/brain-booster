package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.WeakFlashcardDTO;
import com.brainbooster.gameresult.analytics.dto.GameAnalyticsSummaryDTO;
import com.brainbooster.gameresult.analytics.dto.GameProgressPointDTO;
import com.brainbooster.gameresult.analytics.dto.QuestionTypeAnalyticsDTO;
import com.brainbooster.gameresult.attempt.GameAttempt;
import com.brainbooster.gameresult.attempt.GameAttemptRepository;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import com.brainbooster.gameresult.questionresult.GameQuestionResultRepository;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        List<GameAttempt> attempts = gameAttemptRepository
                .findByUserIdAndSetIdOrderByCompletedAtAsc(
                        currentUser.userId(),
                        setId);

        long totalAttempts = attempts.size();

        if (totalAttempts == 0) {
            return new GameAnalyticsSummaryDTO(
                    0L,
                    0.0,
                    0,
                    0.0,
                    null,
                    0.0
            );
        }

        int totalScore = attempts.stream()
                .mapToInt(GameAttempt::getScore)
                .sum();

        int totalQuestions = attempts.stream()
                .mapToInt(GameAttempt::getTotalQuestions)
                .sum();

        double averageScore = attempts.stream()
                .mapToInt(GameAttempt::getScore)
                .average()
                .orElse(0.0);

        int bestScore = attempts.stream()
                .mapToInt(GameAttempt::getScore)
                .max()
                .orElse(0);

        double averageDuration = attempts.stream()
                .filter(attempt -> attempt.getDurationSeconds() != null)
                .mapToInt(GameAttempt::getDurationSeconds)
                .average()
                .orElse(0.0);

        Instant lastAttemptAt = attempts.stream()
                .map(GameAttempt::getCompletedAt)
                .max(Instant::compareTo)
                .orElse(null);

        return new GameAnalyticsSummaryDTO(
                totalAttempts,
                roundToTwoDecimals(averageScore),
                bestScore,
                roundToTwoDecimals(averageDuration),
                lastAttemptAt,
                calculateAccuracyPercentage(
                        totalScore,
                        totalQuestions
                )
        );
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

        List<GameQuestionResult> questionResults = gameQuestionResultRepository
                .findByUserIdAndSetIdOrderByAnsweredAtDesc(
                        currentUser.userId(),
                        setId
                );

        return weakFlashcardAnalyzer.analyze(questionResults);
    }

    @Transactional(readOnly = true)
    public List<QuestionTypeAnalyticsDTO> getMySetQuestionTypeAnalytics(
            Long setId
    ) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        List<GameQuestionResult> questionResults = gameQuestionResultRepository
                .findByUserIdAndSetIdOrderByAnsweredAtDesc(
                        currentUser.userId(),
                        setId
                );

        return questionTypeAnalyzer.analyze(questionResults);
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
                attempt.getMode()
        );
    }

    private static double calculateAccuracyPercentage(
            int correctAnswers,
            int totalQuestions
    ) {
        if (totalQuestions == 0) {
            return 0.0;
        }

        double accuracyPercentage =
                ((double) correctAnswers / totalQuestions) * 100;

        return roundToTwoDecimals(accuracyPercentage);
    }

    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}