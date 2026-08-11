package com.brainbooster.gameresult.analytics;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.gameresult.WeakFlashcardDTO;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<WeakFlashcardDTO> analyze(
            List<GameQuestionResult> questionResults
    ) {
        Map<Long, WeakFlashcardStats> statsByFlashcardId = new HashMap<>();

        for (GameQuestionResult questionResult : questionResults) {
            Long flashcardId =
                    questionResult.getFlashcard().getFlashcardId();

            statsByFlashcardId
                    .computeIfAbsent(
                            flashcardId,
                            ignored -> new WeakFlashcardStats(
                                    questionResult.getFlashcard()
                            )
                    )
                    .addResult(questionResult);
        }

        return statsByFlashcardId.values()
                .stream()
                .map(WeakFlashcardStats::toDto)
                .filter(this::isWeak)
                .sorted(WEAKEST_FIRST)
                .toList();
    }

    private boolean isWeak(WeakFlashcardDTO flashcard) {
        return flashcard.incorrectAnswers() > 0
                || flashcard.totalMistakes() > 0;
    }

    private static class WeakFlashcardStats {

        private final Flashcard flashcard;

        private long totalAnswers;
        private long correctAnswers;
        private long incorrectAnswers;
        private int totalMistakes;
        private Instant lastAnsweredAt;

        private WeakFlashcardStats(Flashcard flashcard) {
            this.flashcard = flashcard;
        }

        private void addResult(
                GameQuestionResult questionResult
        ) {
            totalAnswers++;

            updateAnswerCounters(questionResult);
            updateTotalMistakes(questionResult);
            updateLastAnsweredAt(questionResult.getAnsweredAt());
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

        private void updateTotalMistakes(
                GameQuestionResult questionResult
        ) {
            totalMistakes += questionResult.getMistakesCount() == null
                    ? 0
                    : questionResult.getMistakesCount();
        }

        private void updateLastAnsweredAt(Instant answeredAt) {
            if (
                    lastAnsweredAt == null
                            || answeredAt.isAfter(lastAnsweredAt)
            ) {
                lastAnsweredAt = answeredAt;
            }
        }

        private WeakFlashcardDTO toDto() {
            return new WeakFlashcardDTO(
                    flashcard.getFlashcardId(),
                    flashcard.getTerm(),
                    flashcard.getDefinition(),
                    totalAnswers,
                    correctAnswers,
                    incorrectAnswers,
                    totalMistakes,
                    calculateAccuracyPercentage(),
                    lastAnsweredAt
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
