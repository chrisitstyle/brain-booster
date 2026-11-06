package com.brainbooster.gameresult.questionresult;

import com.brainbooster.gameresult.GameQuestionType;

public interface QuestionTypeStatisticsProjection {

    GameQuestionType getQuestionType();

    Long getTotalAnswers();

    Long getCorrectAnswers();
}
