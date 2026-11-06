
package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.WeakFlashcardDTO;
import com.brainbooster.gameresult.questionresult.WeakFlashcardStatisticsProjection;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeakFlashcardAnalyzerTest {

    private final WeakFlashcardAnalyzer weakFlashcardAnalyzer = new WeakFlashcardAnalyzer();

    @Test
    void analyzeAggregates_shouldCalculateStatistics_whenFlashcardHasIncorrectAnswers() {
        // given
        Instant lastAnsweredAt = Instant.parse("2026-08-02T10:00:00Z");

        WeakFlashcardStatisticsProjection flashcardStatistics = createFlashcardStatistics(
                        1L,
                        "Weak term",
                        "Weak definition",
                        2L,
                        1L,
                        2L,
                        lastAnsweredAt);

        // when
        List<WeakFlashcardDTO> actualFlashcards = weakFlashcardAnalyzer.analyzeAggregates(
                        List.of(flashcardStatistics));

        // then
        WeakFlashcardDTO expectedFlashcard = new WeakFlashcardDTO(
                        1L,
                        "Weak term",
                        "Weak definition",
                        2L,
                        1L,
                        1L,
                        2,
                        50.0,
                        lastAnsweredAt);

        assertThat(actualFlashcards).containsExactly(expectedFlashcard);
    }

    @Test
    void analyzeAggregates_shouldExcludeFlashcard_whenAllAnswersAreCorrectWithoutMistakes() {
        // given
        WeakFlashcardStatisticsProjection flashcardStatistics = createFlashcardStatistics(
                        1L,
                        "cat",
                        "kot",
                        3L,
                        3L,
                        0L,
                        Instant.parse("2026-08-01T10:00:00Z"));

        // when
        List<WeakFlashcardDTO> actualFlashcards = weakFlashcardAnalyzer.analyzeAggregates(
                        List.of(flashcardStatistics));

        // then
        assertThat(actualFlashcards).isEmpty();
    }

    @Test
    void analyzeAggregates_shouldIncludeFlashcard_whenCorrectAnswersHaveMistakes() {
        // given
        Instant lastAnsweredAt = Instant.parse("2026-08-02T10:00:00Z");

        WeakFlashcardStatisticsProjection flashcardStatistics =
                createFlashcardStatistics(
                        25L,
                        "cat",
                        "kot",
                        2L,
                        2L,
                        3L,
                        lastAnsweredAt);

        // when
        List<WeakFlashcardDTO> actualFlashcards = weakFlashcardAnalyzer.analyzeAggregates(
                        List.of(flashcardStatistics));

        // then
        WeakFlashcardDTO expectedFlashcard = new WeakFlashcardDTO(
                        25L,
                        "cat",
                        "kot",
                        2L,
                        2L,
                        0L,
                        3,
                        100.0,
                        lastAnsweredAt);

        assertThat(actualFlashcards)
                .containsExactly(expectedFlashcard);
    }

    @Test
    void analyzeAggregates_shouldSortByIncorrectAnswers_whenFlashcardsHaveDifferentErrorCounts() {
        // given
        Instant lastAnsweredAt = Instant.parse("2026-08-01T10:00:00Z");

        WeakFlashcardStatisticsProjection weakerFlashcardStatistics = createFlashcardStatistics(
                        1L,
                        "Weaker",
                        "Definition",
                        2L,
                        0L,
                        2L,
                        lastAnsweredAt);

        WeakFlashcardStatisticsProjection lessWeakFlashcardStatistics = createFlashcardStatistics(
                        2L,
                        "Less weak",
                        "Definition",
                        1L,
                        0L,
                        5L,
                        lastAnsweredAt);

        // when
        List<WeakFlashcardDTO> actualFlashcards = weakFlashcardAnalyzer.analyzeAggregates(
                        List.of(lessWeakFlashcardStatistics,
                                weakerFlashcardStatistics));

        // then
        assertThat(actualFlashcards)
                .extracting(WeakFlashcardDTO::flashcardId)
                .containsExactly(1L, 2L);
    }

    @Test
    void analyzeAggregates_shouldSortByTotalMistakes_whenIncorrectAnswerCountsAreEqual() {
        // given
        Instant lastAnsweredAt = Instant.parse("2026-08-01T10:00:00Z");

        WeakFlashcardStatisticsProjection moreMistakesStatistics =
                createFlashcardStatistics(
                        1L,
                        "More mistakes",
                        "Definition",
                        2L,
                        1L,
                        5L,
                        lastAnsweredAt);

        WeakFlashcardStatisticsProjection fewerMistakesStatistics = createFlashcardStatistics(
                        2L,
                        "Fewer mistakes",
                        "Definition",
                        2L,
                        1L,
                        2L,
                        lastAnsweredAt);

        // when
        List<WeakFlashcardDTO> actualFlashcards = weakFlashcardAnalyzer.analyzeAggregates(
                        List.of(fewerMistakesStatistics, moreMistakesStatistics));

        // then
        assertThat(actualFlashcards)
                .extracting(WeakFlashcardDTO::flashcardId)
                .containsExactly(1L, 2L);
    }

    @Test
    void analyzeAggregates_shouldSortByLastAnsweredAt_whenDifficultyIsEqual() {
        // given
        Instant earlierAnsweredAt = Instant.parse("2026-08-01T10:00:00Z");

        Instant laterAnsweredAt = Instant.parse("2026-08-02T10:00:00Z");

        WeakFlashcardStatisticsProjection earlierFlashcardStatistics = createFlashcardStatistics(
                        1L,
                        "Earlier",
                        "Definition",
                        2L,
                        1L,
                        1L,
                        earlierAnsweredAt);

        WeakFlashcardStatisticsProjection laterFlashcardStatistics = createFlashcardStatistics(
                        2L,
                        "Later",
                        "Definition",
                        2L,
                        1L,
                        1L,
                        laterAnsweredAt);

        // when
        List<WeakFlashcardDTO> actualFlashcards = weakFlashcardAnalyzer.analyzeAggregates(
                        List.of(earlierFlashcardStatistics,
                                laterFlashcardStatistics));

        // then
        assertThat(actualFlashcards)
                .extracting(WeakFlashcardDTO::flashcardId)
                .containsExactly(2L, 1L);
    }

    @Test
    void analyzeAggregates_shouldReturnEmptyList_whenStatisticsAreEmpty() {
        // given
        List<WeakFlashcardStatisticsProjection> flashcardStatistics = List.of();

        // when
        List<WeakFlashcardDTO> actualFlashcards = weakFlashcardAnalyzer.analyzeAggregates(
                        flashcardStatistics);

        // then
        assertThat(actualFlashcards).isEmpty();
    }

    private WeakFlashcardStatisticsProjection createFlashcardStatistics(
            Long flashcardId,
            String term,
            String definition,
            long totalAnswers,
            long correctAnswers,
            long totalMistakes,
            Instant lastAnsweredAt
    ) {
        WeakFlashcardStatisticsProjection flashcardStatistics =
                mock(WeakFlashcardStatisticsProjection.class);

        when(flashcardStatistics.getFlashcardId())
                .thenReturn(flashcardId);

        when(flashcardStatistics.getTerm())
                .thenReturn(term);

        when(flashcardStatistics.getDefinition())
                .thenReturn(definition);

        when(flashcardStatistics.getTotalAnswers())
                .thenReturn(totalAnswers);

        when(flashcardStatistics.getCorrectAnswers())
                .thenReturn(correctAnswers);

        when(flashcardStatistics.getTotalMistakes())
                .thenReturn(totalMistakes);

        when(flashcardStatistics.getLastAnsweredAt())
                .thenReturn(lastAnsweredAt);

        return flashcardStatistics;
    }
}
