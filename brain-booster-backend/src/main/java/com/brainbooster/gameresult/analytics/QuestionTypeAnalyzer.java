package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.analytics.dto.QuestionTypeAnalyticsDTO;
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
}
