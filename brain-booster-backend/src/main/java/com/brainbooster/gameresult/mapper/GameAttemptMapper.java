package com.brainbooster.gameresult.mapper;

import com.brainbooster.gameresult.attempt.GameAttempt;
import com.brainbooster.gameresult.dto.GameAttemptDTO;
import com.brainbooster.gameresult.dto.GameAttemptSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameAttemptMapper {

    private final GameQuestionResultMapper gameQuestionResultMapper;

    public GameAttemptDTO toDto(GameAttempt attempt) {
        return new GameAttemptDTO(
                attempt.getAttemptId(),
                attempt.getUser().getUserId(),
                attempt.getSet().getSetId(),
                attempt.getMode(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                attempt.getDurationSeconds(),
                attempt.getCompletedAt(),
                attempt.getQuestionResults().stream()
                        .map(gameQuestionResultMapper::toDto)
                        .toList()
        );
    }

    public GameAttemptSummaryDTO toSummaryDto(GameAttempt attempt) {
        return new GameAttemptSummaryDTO(
                attempt.getAttemptId(),
                attempt.getUser().getUserId(),
                attempt.getSet().getSetId(),
                attempt.getMode(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                attempt.getDurationSeconds(),
                attempt.getCompletedAt()
        );
    }
}
