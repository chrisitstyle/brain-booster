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
import com.brainbooster.user.User;
import com.brainbooster.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameAnalyticsService {

    private final GameAttemptRepository gameAttemptRepository;
    private final GameQuestionResultRepository gameQuestionResultRepository;

    @Transactional(readOnly = true)
    public GameAnalyticsSummaryDTO getMySetSummary(Long setId) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        List<GameAttempt> attempts = gameAttemptRepository
                .findByUserIdAndSetIdOrderByCompletedAtAsc(
                        authenticatedUser.getUserId(),
                        setId
                );

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

        LocalDateTime lastAttemptAt = attempts.stream()
                .map(GameAttempt::getCompletedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        double accuracyPercentage = totalQuestions > 0
                ? ((double) totalScore / totalQuestions) * 100
                : 0.0;

        return new GameAnalyticsSummaryDTO(
                totalAttempts,
                roundTwoDecimals(averageScore),
                bestScore,
                roundTwoDecimals(averageDuration),
                lastAttemptAt,
                roundTwoDecimals(accuracyPercentage)
        );
    }

    @Transactional(readOnly = true)
    public List<GameProgressPointDTO> getMySetProgress(Long setId) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        return gameAttemptRepository
                .findByUserIdAndSetIdOrderByCompletedAtAsc(
                        authenticatedUser.getUserId(),
                        setId
                )
                .stream()
                .map(this::toProgressPoint)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WeakFlashcardDTO> getMySetWeakFlashcards(Long setId) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        List<GameQuestionResult> questionResults = gameQuestionResultRepository
                .findByUserIdAndSetIdOrderByAnsweredAtDesc(
                        authenticatedUser.getUserId(),
                        setId
                );

        Map<Long, WeakFlashcardStats> statsByFlashcardId = new java.util.HashMap<>();

        for (GameQuestionResult questionResult : questionResults) {
            Long flashcardId = questionResult.getFlashcard().getFlashcardId();

            statsByFlashcardId
                    .computeIfAbsent(
                            flashcardId,
                            ignored -> new WeakFlashcardStats(questionResult.getFlashcard())
                    )
                    .add(questionResult);
        }

        return statsByFlashcardId.values()
                .stream()
                .map(WeakFlashcardStats::toDto)
                .filter(dto -> dto.incorrectAnswers() > 0 || dto.totalMistakes() > 0)
                .sorted(this::compareWeakFlashcards)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionTypeAnalyticsDTO> getMySetQuestionTypeAnalytics(Long setId) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        List<GameQuestionResult> questionResults = gameQuestionResultRepository
                .findByUserIdAndSetIdOrderByAnsweredAtDesc(
                        authenticatedUser.getUserId(),
                        setId
                );

        Map<GameQuestionType, QuestionTypeStats> statsByQuestionType =
                new EnumMap<>(GameQuestionType.class);

        for (GameQuestionResult questionResult : questionResults) {
            statsByQuestionType
                    .computeIfAbsent(
                            questionResult.getQuestionType(),
                            QuestionTypeStats::new
                    )
                    .add(questionResult);
        }

        return statsByQuestionType.values()
                .stream()
                .map(QuestionTypeStats::toDto)
                .sorted(Comparator.comparing(dto -> dto.questionType().ordinal()))
                .toList();
    }

    private GameProgressPointDTO toProgressPoint(GameAttempt attempt) {
        double percentage = attempt.getTotalQuestions() > 0
                ? ((double) attempt.getScore() / attempt.getTotalQuestions()) * 100
                : 0.0;

        return new GameProgressPointDTO(
                attempt.getAttemptId(),
                attempt.getCompletedAt(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                roundTwoDecimals(percentage),
                attempt.getDurationSeconds(),
                attempt.getMode()
        );
    }

    private int compareWeakFlashcards(
            WeakFlashcardDTO first,
            WeakFlashcardDTO second
    ) {
        int incorrectCompare = Long.compare(
                second.incorrectAnswers(),
                first.incorrectAnswers()
        );

        if (incorrectCompare != 0) {
            return incorrectCompare;
        }

        int mistakesCompare = Integer.compare(
                second.totalMistakes(),
                first.totalMistakes()
        );

        if (mistakesCompare != 0) {
            return mistakesCompare;
        }

        if (first.lastAnsweredAt() == null && second.lastAnsweredAt() == null) {
            return 0;
        }

        if (first.lastAnsweredAt() == null) {
            return 1;
        }

        if (second.lastAnsweredAt() == null) {
            return -1;
        }

        return second.lastAnsweredAt().compareTo(first.lastAnsweredAt());
    }

    private static double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class WeakFlashcardStats {

        private final Flashcard flashcard;
        private long totalAnswers;
        private long correctAnswers;
        private long incorrectAnswers;
        private int totalMistakes;
        private LocalDateTime lastAnsweredAt;

        private WeakFlashcardStats(Flashcard flashcard) {
            this.flashcard = flashcard;
        }

        private void add(GameQuestionResult questionResult) {
            totalAnswers++;

            if (Boolean.TRUE.equals(questionResult.getWasCorrect())) {
                correctAnswers++;
            } else {
                incorrectAnswers++;
            }

            totalMistakes += questionResult.getMistakesCount() == null
                    ? 0
                    : questionResult.getMistakesCount();

            if (
                    lastAnsweredAt == null ||
                            questionResult.getAnsweredAt().isAfter(lastAnsweredAt)
            ) {
                lastAnsweredAt = questionResult.getAnsweredAt();
            }
        }

        private WeakFlashcardDTO toDto() {
            double accuracyPercentage = totalAnswers > 0
                    ? ((double) correctAnswers / totalAnswers) * 100
                    : 0.0;

            return new WeakFlashcardDTO(
                    flashcard.getFlashcardId(),
                    flashcard.getTerm(),
                    flashcard.getDefinition(),
                    totalAnswers,
                    correctAnswers,
                    incorrectAnswers,
                    totalMistakes,
                    roundTwoDecimals(accuracyPercentage),
                    lastAnsweredAt
            );
        }
    }

    private static class QuestionTypeStats {

        private final GameQuestionType questionType;
        private long totalAnswers;
        private long correctAnswers;
        private long incorrectAnswers;

        private QuestionTypeStats(GameQuestionType questionType) {
            this.questionType = questionType;
        }

        private void add(GameQuestionResult questionResult) {
            totalAnswers++;

            if (Boolean.TRUE.equals(questionResult.getWasCorrect())) {
                correctAnswers++;
            } else {
                incorrectAnswers++;
            }
        }

        private QuestionTypeAnalyticsDTO toDto() {
            double accuracyPercentage = totalAnswers > 0
                    ? ((double) correctAnswers / totalAnswers) * 100
                    : 0.0;

            return new QuestionTypeAnalyticsDTO(
                    questionType,
                    totalAnswers,
                    correctAnswers,
                    incorrectAnswers,
                    roundTwoDecimals(accuracyPercentage)
            );
        }
    }
}
