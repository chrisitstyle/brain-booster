package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.WeakFlashcardDTO;
import com.brainbooster.gameresult.questionresult.WeakFlashcardStatisticsProjection;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class WeakFlashcardAnalyzer {

    private static final Comparator<WeakFlashcardDTO> WEAKEST_FIRST = Comparator
            .comparingLong(WeakFlashcardDTO::incorrectAnswers)
            .reversed()
            .thenComparing(
                    Comparator.comparingInt(
                            WeakFlashcardDTO::totalMistakes
                    ).reversed()
            )
            .thenComparing(
                    WeakFlashcardDTO::lastAnsweredAt,
                    Comparator.nullsLast(
                            Comparator.reverseOrder()
                    )
            );

    public List<WeakFlashcardDTO> analyzeAggregates(
            List<WeakFlashcardStatisticsProjection> flashcardStatistics
    ) {
        return flashcardStatistics.stream()
                .map(this::mapStatisticsToDto)
                .filter(this::isWeak)
                .sorted(WEAKEST_FIRST)
                .toList();
    }

    private WeakFlashcardDTO mapStatisticsToDto(
            WeakFlashcardStatisticsProjection flashcardStatistics
    ) {
        long totalAnswers = flashcardStatistics.getTotalAnswers();
        long correctAnswers = flashcardStatistics.getCorrectAnswers();
        long incorrectAnswers = totalAnswers - correctAnswers;

        int totalMistakes = Math.toIntExact(flashcardStatistics.getTotalMistakes());

        double accuracyPercentage = totalAnswers == 0
                ? 0.0
                : Math.round(
                ((double) correctAnswers / totalAnswers) * 10000.0) / 100.0;

        return new WeakFlashcardDTO(
                flashcardStatistics.getFlashcardId(),
                flashcardStatistics.getTerm(),
                flashcardStatistics.getDefinition(),
                totalAnswers,
                correctAnswers,
                incorrectAnswers,
                totalMistakes,
                accuracyPercentage,
                flashcardStatistics.getLastAnsweredAt());
    }


    private boolean isWeak(WeakFlashcardDTO flashcard) {
        return flashcard.incorrectAnswers() > 0
                || flashcard.totalMistakes() > 0;
    }
}
