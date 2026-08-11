package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.analytics.dto.QuestionTypeAnalyticsDTO;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionTypeAnalyzerTest {

    private final QuestionTypeAnalyzer questionTypeAnalyzer =
            new QuestionTypeAnalyzer();

    @Test
    void analyze_ShouldAggregateResultsByQuestionType() {
        // given
        GameQuestionType questionType = GameQuestionType.values()[0];

        GameQuestionResult firstCorrectResult = createQuestionResult(questionType, true);

        GameQuestionResult secondCorrectResult = createQuestionResult(questionType, true);

        GameQuestionResult incorrectResult = createQuestionResult(questionType, false);

        // when
        List<QuestionTypeAnalyticsDTO> result = questionTypeAnalyzer.analyze(
                        List.of(
                                firstCorrectResult,
                                secondCorrectResult,
                                incorrectResult));

        // then
        QuestionTypeAnalyticsDTO expected = new QuestionTypeAnalyticsDTO(
                        questionType,
                        3L,
                        2L,
                        1L,
                        66.67);

        assertThat(result)
                .containsExactly(expected);
    }

    @Test
    void analyze_ShouldSeparateDifferentQuestionTypes() {
        // given
        GameQuestionType firstType = GameQuestionType.values()[0];

        GameQuestionType secondType = GameQuestionType.values()[1];

        GameQuestionResult firstTypeResult = createQuestionResult(firstType, true);

        GameQuestionResult secondTypeResult = createQuestionResult(secondType, false);

        // when
        List<QuestionTypeAnalyticsDTO> result = questionTypeAnalyzer.analyze(
                        List.of(
                                secondTypeResult,
                                firstTypeResult
                        )
                );

        // then
        assertThat(result)
                .extracting(QuestionTypeAnalyticsDTO::questionType)
                .containsExactly(
                        firstType,
                        secondType);
    }

    @Test
    void analyze_ShouldCalculateAccuracyPercentage() {
        // given
        GameQuestionType questionType = GameQuestionType.values()[0];

        GameQuestionResult correctResult = createQuestionResult(questionType, true);

        GameQuestionResult incorrectResult = createQuestionResult(questionType, false);

        // when
        List<QuestionTypeAnalyticsDTO> result = questionTypeAnalyzer.analyze(
                        List.of(
                                correctResult,
                                incorrectResult));

        // then
        assertThat(result)
                .singleElement()
                .extracting(
                        QuestionTypeAnalyticsDTO::accuracyPercentage
                )
                .isEqualTo(50.0);
    }

    @Test
    void analyze_ShouldReturnEmptyList_WhenNoResultsExist() {
        // when
        List<QuestionTypeAnalyticsDTO> result = questionTypeAnalyzer.analyze(List.of());

        // then
        assertThat(result).isEmpty();
    }

    private GameQuestionResult createQuestionResult(
            GameQuestionType questionType,
            boolean wasCorrect
    ) {
        GameQuestionResult result = mock(GameQuestionResult.class);

        when(result.getQuestionType())
                .thenReturn(questionType);

        when(result.getWasCorrect())
                .thenReturn(wasCorrect);

        return result;
    }
}
