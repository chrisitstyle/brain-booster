
package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.analytics.dto.QuestionTypeAnalyticsDTO;
import com.brainbooster.gameresult.questionresult.QuestionTypeStatisticsProjection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionTypeAnalyzerTest {

    private final QuestionTypeAnalyzer questionTypeAnalyzer = new QuestionTypeAnalyzer();

    @Test
    void analyzeAggregates_shouldCalculateStatistics_whenQuestionTypeExists() {
        // given
        GameQuestionType questionType = GameQuestionType.MULTIPLE_CHOICE;

        QuestionTypeStatisticsProjection questionTypeStatistics = createQuestionTypeStatistics(questionType, 3L, 2L);

        // when
        List<QuestionTypeAnalyticsDTO> actualStatistics = questionTypeAnalyzer.analyzeAggregates(
                        List.of(questionTypeStatistics));

        // then
        QuestionTypeAnalyticsDTO expectedStatistics = new QuestionTypeAnalyticsDTO(
                        questionType,
                        3L,
                        2L,
                        1L,
                        66.67);

        assertThat(actualStatistics)
                .containsExactly(expectedStatistics);
    }

    @Test
    void analyzeAggregates_shouldSeparateQuestionTypes_whenMultipleTypesExist() {
        // given
        GameQuestionType firstQuestionType = GameQuestionType.values()[0];

        GameQuestionType secondQuestionType = GameQuestionType.values()[1];

        QuestionTypeStatisticsProjection firstTypeStatistics = createQuestionTypeStatistics(
                        firstQuestionType,
                        3L,
                        2L);

        QuestionTypeStatisticsProjection secondTypeStatistics = createQuestionTypeStatistics(
                        secondQuestionType,
                        2L,
                        0L);

        // when
        List<QuestionTypeAnalyticsDTO> actualStatistics = questionTypeAnalyzer.analyzeAggregates(
                        List.of(
                                secondTypeStatistics,
                                firstTypeStatistics));

        // then
        QuestionTypeAnalyticsDTO expectedFirstType = new QuestionTypeAnalyticsDTO(
                        firstQuestionType,
                        3L,
                        2L,
                        1L,
                        66.67);

        QuestionTypeAnalyticsDTO expectedSecondType = new QuestionTypeAnalyticsDTO(
                        secondQuestionType,
                        2L,
                        0L,
                        2L,
                        0.0);

        assertThat(actualStatistics)
                .containsExactly(
                        expectedFirstType,
                        expectedSecondType);
    }

    @Test
    void analyzeAggregates_shouldCalculateAccuracy_whenHalfOfAnswersAreCorrect() {
        // given
        QuestionTypeStatisticsProjection questionTypeStatistics = createQuestionTypeStatistics(
                        GameQuestionType.MULTIPLE_CHOICE,
                        2L,
                        1L);

        // when
        List<QuestionTypeAnalyticsDTO> actualStatistics = questionTypeAnalyzer.analyzeAggregates(
                        List.of(questionTypeStatistics));

        // then
        assertThat(actualStatistics)
                .singleElement()
                .satisfies(statistics -> {
                    assertThat(statistics.totalAnswers())
                            .isEqualTo(2L);

                    assertThat(statistics.correctAnswers())
                            .isEqualTo(1L);

                    assertThat(statistics.incorrectAnswers())
                            .isEqualTo(1L);

                    assertThat(statistics.accuracyPercentage())
                            .isEqualTo(50.0);
                });
    }

    @Test
    void analyzeAggregates_shouldReturnEmptyList_whenStatisticsAreEmpty() {
        // given
        List<QuestionTypeStatisticsProjection> questionTypeStatistics = List.of();

        // when
        List<QuestionTypeAnalyticsDTO> actualStatistics = questionTypeAnalyzer.analyzeAggregates(
                        questionTypeStatistics);

        // then
        assertThat(actualStatistics).isEmpty();
    }

    private QuestionTypeStatisticsProjection createQuestionTypeStatistics(
            GameQuestionType questionType,
            long totalAnswers,
            long correctAnswers
    ) {
        QuestionTypeStatisticsProjection questionTypeStatistics = mock(QuestionTypeStatisticsProjection.class);

        when(questionTypeStatistics.getQuestionType())
                .thenReturn(questionType);

        when(questionTypeStatistics.getTotalAnswers())
                .thenReturn(totalAnswers);

        when(questionTypeStatistics.getCorrectAnswers())
                .thenReturn(correctAnswers);

        return questionTypeStatistics;
    }
}
