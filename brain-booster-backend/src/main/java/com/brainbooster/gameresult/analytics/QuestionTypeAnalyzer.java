package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.analytics.dto.QuestionTypeAnalyticsDTO;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class QuestionTypeAnalyzer {

    public List<QuestionTypeAnalyticsDTO> analyze(
            List<GameQuestionResult> questionResults
    ) {
        Map<GameQuestionType, QuestionTypeStats> statsByQuestionType = new EnumMap<>(GameQuestionType.class);

        for (GameQuestionResult questionResult : questionResults) {
            statsByQuestionType
                    .computeIfAbsent(
                            questionResult.getQuestionType(),
                            QuestionTypeStats::new
                    )
                    .addResult(questionResult);
        }

        return statsByQuestionType.values()
                .stream()
                .map(QuestionTypeStats::toDto)
                .sorted(
                        Comparator.comparing(
                                dto -> dto.questionType().ordinal()
                        )
                )
                .toList();
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
