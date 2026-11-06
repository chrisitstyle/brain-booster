package com.brainbooster.gameresult.attempt;

import java.time.Instant;

public interface GameAttemptSummaryProjection {

    Long getTotalAttempts();

    Long getTotalScore();

    Long getTotalQuestions();

    Double getAverageScore();

    Integer getBestScore();

    Double getAverageDurationSeconds();

    Instant getLastCompletedAt();
}
