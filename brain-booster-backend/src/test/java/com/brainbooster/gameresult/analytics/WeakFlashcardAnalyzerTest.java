package com.brainbooster.gameresult.analytics;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.gameresult.WeakFlashcardDTO;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeakFlashcardAnalyzerTest {

    private final WeakFlashcardAnalyzer weakFlashcardAnalyzer = new WeakFlashcardAnalyzer();

    @Test
    void analyze_ShouldAggregateWeakFlashcardResults() {
        // given
        Instant firstAnsweredAt = Instant.parse("2026-08-01T10:00:00Z");

        Instant secondAnsweredAt = Instant.parse("2026-08-02T10:00:00Z");

        Flashcard flashcard = mock(Flashcard.class);

        when(flashcard.getFlashcardId()).thenReturn(1L);
        when(flashcard.getTerm()).thenReturn("Weak term");
        when(flashcard.getDefinition()).thenReturn("Weak definition");

        GameQuestionResult correctResult = createQuestionResult(
                flashcard,
                true,
                0,
                firstAnsweredAt);

        GameQuestionResult incorrectResult = createQuestionResult(
                flashcard,
                false,
                2,
                secondAnsweredAt);

        // when
        List<WeakFlashcardDTO> result = weakFlashcardAnalyzer.analyze(
                List.of(correctResult, incorrectResult)
        );

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

        assertThat(result).containsExactly(expected);
    }

    @Test
    void analyze_ShouldExcludeFlashcardWithoutIncorrectAnswersOrMistakes() {
        // given
        Flashcard flashcard = mock(Flashcard.class);

        when(flashcard.getFlashcardId()).thenReturn(1L);

        GameQuestionResult correctResult = createQuestionResult(
                flashcard,
                true,
                0,
                Instant.parse("2026-08-01T10:00:00Z"));

        // when
        List<WeakFlashcardDTO> result = weakFlashcardAnalyzer.analyze(
                        List.of(correctResult));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void analyze_ShouldIncludeCorrectFlashcard_WhenMistakesWereMade() {
        // given
        Instant answeredAt = Instant.parse("2026-08-01T10:00:00Z");

        Flashcard flashcard = mock(Flashcard.class);

        when(flashcard.getFlashcardId()).thenReturn(1L);
        when(flashcard.getTerm()).thenReturn("Term");
        when(flashcard.getDefinition()).thenReturn("Definition");

        GameQuestionResult resultWithMistakes = createQuestionResult(
                flashcard,
                true,
                2,
                answeredAt);

        // when
        List<WeakFlashcardDTO> result = weakFlashcardAnalyzer.analyze(
                        List.of(resultWithMistakes));

        // then
        WeakFlashcardDTO expected = new WeakFlashcardDTO(
                1L,
                "Term",
                "Definition",
                1L,
                1L,
                0L,
                2,
                100.0,
                answeredAt);

        assertThat(result).containsExactly(expected);
    }

    @Test
    void analyze_ShouldSortWeakestFlashcardsFirst() {
        // given
        Instant answeredAt = Instant.parse("2026-08-01T10:00:00Z");

        Flashcard weakerFlashcard = mock(Flashcard.class);
        Flashcard lessWeakFlashcard = mock(Flashcard.class);

        when(weakerFlashcard.getFlashcardId()).thenReturn(1L);
        when(weakerFlashcard.getTerm()).thenReturn("Weaker");
        when(weakerFlashcard.getDefinition()).thenReturn("Definition");

        when(lessWeakFlashcard.getFlashcardId()).thenReturn(2L);
        when(lessWeakFlashcard.getTerm()).thenReturn("Less weak");
        when(lessWeakFlashcard.getDefinition()).thenReturn("Definition");

        GameQuestionResult firstIncorrect = createQuestionResult(
                weakerFlashcard,
                false,
                1,
                answeredAt);

        GameQuestionResult secondIncorrect = createQuestionResult(
                weakerFlashcard,
                false,
                1,
                answeredAt);

        GameQuestionResult lessWeakIncorrect = createQuestionResult(
                lessWeakFlashcard,
                false,
                5,
                answeredAt);

        // when
        List<WeakFlashcardDTO> result = weakFlashcardAnalyzer.analyze(
                        List.of(
                                lessWeakIncorrect,
                                firstIncorrect,
                                secondIncorrect));

        // then
        assertThat(result)
                .extracting(WeakFlashcardDTO::flashcardId)
                .containsExactly(1L, 2L);
    }

    @Test
    void analyze_ShouldReturnEmptyList_WhenNoResultsExist() {
        // when
        List<WeakFlashcardDTO> result = weakFlashcardAnalyzer.analyze(List.of());

        // then
        assertThat(result).isEmpty();
    }

    private GameQuestionResult createQuestionResult(
            Flashcard flashcard,
            boolean wasCorrect,
            Integer mistakesCount,
            Instant answeredAt
    ) {
        GameQuestionResult result = mock(GameQuestionResult.class);

        when(result.getFlashcard()).thenReturn(flashcard);
        when(result.getWasCorrect()).thenReturn(wasCorrect);
        when(result.getMistakesCount()).thenReturn(mistakesCount);
        when(result.getAnsweredAt()).thenReturn(answeredAt);

        return result;
    }
}
