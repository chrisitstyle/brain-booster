package com.brainbooster.gameresult.mapper;

import com.brainbooster.gameresult.dto.GameQuestionResultDTO;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import org.springframework.stereotype.Component;

@Component
public class GameQuestionResultMapper {

    public GameQuestionResultDTO toDto(GameQuestionResult questionResult) {
        return new GameQuestionResultDTO(
                questionResult.getQuestionResultId(),
                questionResult.getAttempt().getAttemptId(),
                questionResult.getFlashcard().getFlashcardId(),
                questionResult.getQuestionKey(),
                questionResult.getQuestionOrder(),
                questionResult.getQuestionType(),
                questionResult.getAnswerWith(),
                questionResult.getPrompt(),
                questionResult.getUserAnswer(),
                questionResult.getCorrectAnswer(),
                questionResult.getWasCorrect(),
                questionResult.getMistakesCount(),
                questionResult.getAnsweredAt()
        );
    }
}
