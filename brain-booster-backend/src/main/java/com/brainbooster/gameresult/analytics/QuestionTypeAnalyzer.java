package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.analytics.dto.QuestionTypeAnalyticsDTO;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import com.brainbooster.gameresult.questionresult.QuestionTypeStatisticsProjection;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class QuestionTypeAnalyzer {

    public List<QuestionTypeAnalyticsDTO> analyzeAggregates(
            List<QuestionTypeStatisticsProjection> questionTypeStatistics
    ) {
        return questionTypeStatistics.stream()
                .map(this::mapStatisticsToDto)
                .sorted(
                        Comparator.comparingInt(
                                statistics -> statistics.questionType().ordinal()
                        )
                )
                .toList();
    }

    private QuestionTypeAnalyticsDTO mapStatisticsToDto(
            QuestionTypeStatisticsProjection questionTypeStatistics
    ) {
        long totalAnswers = questionTypeStatistics.getTotalAnswers();
        long correctAnswers = questionTypeStatistics.getCorrectAnswers();
        long incorrectAnswers = totalAnswers - correctAnswers;

        double accuracyPercentage = totalAnswers == 0
                ? 0.0
                : Math.round(
                ((double) correctAnswers / totalAnswers) * 10000.0) / 100.0;

        return new QuestionTypeAnalyticsDTO(
                questionTypeStatistics.getQuestionType(),
                totalAnswers,
                correctAnswers,
                incorrectAnswers,
                accuracyPercentage);
    }


    private static class QuestionTypeStats {

        private final GameQuestionType questionType;

        private long totalAnswers;
        private long correctAnswers;
        private long incorrectAnswers;

        private QuestionTypeStats(
                GameQuestionType questionType
        ) {
            this.questionType = questionType;
        }

        private void addResult(
                GameQuestionResult questionResult
        ) {
            totalAnswers++;
            updateAnswerCounters(questionResult);
        }

        private void updateAnswerCounters(
                GameQuestionResult questionResult
        ) {
            if (Boolean.TRUE.equals(questionResult.getWasCorrect())) {
                correctAnswers++;
            } else {
                incorrectAnswers++;
            }
        }

        private QuestionTypeAnalyticsDTO toDto() {
            return new QuestionTypeAnalyticsDTO(
                    questionType,
                    totalAnswers,
                    correctAnswers,
                    incorrectAnswers,
                    calculateAccuracyPercentage()
            );
        }

        private double calculateAccuracyPercentage() {
            if (totalAnswers == 0) {
                return 0.0;
            }

            double accuracyPercentage = ((double) correctAnswers / totalAnswers) * 100;

            return Math.round(accuracyPercentage * 100.0) / 100.0;
        }
    }
}
