package com.brainbooster.gameresult.mapper;

import com.brainbooster.gameresult.GameResult;
import com.brainbooster.gameresult.dto.GameResultDTO;
import org.springframework.stereotype.Component;

@Component
public class GameResultMapper {

    public GameResultDTO toDto(GameResult gameResult) {
        return new GameResultDTO(
                gameResult.getResultId(),
                gameResult.getUser().getUserId(),
                gameResult.getSet().getSetId(),
                gameResult.getMode(),
                gameResult.getScore(),
                gameResult.getTotalQuestions(),
                gameResult.getDurationSeconds(),
                gameResult.getCompletedAt()
        );
    }
}