package com.brainbooster.gameresult.questionresult;

import java.time.Instant;

public interface WeakFlashcardStatisticsProjection {

    Long getFlashcardId();

    String getTerm();

    String getDefinition();

    Long getTotalAnswers();

    Long getCorrectAnswers();

    Long getTotalMistakes();

    Instant getLastAnsweredAt();
}
