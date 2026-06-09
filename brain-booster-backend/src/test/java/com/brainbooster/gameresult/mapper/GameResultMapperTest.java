package com.brainbooster.gameresult.mapper;

import com.brainbooster.gameresult.GameMode;
import com.brainbooster.gameresult.GameResult;
import com.brainbooster.gameresult.dto.GameResultDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.brainbooster.utils.TestEntities.createGameResultDTO;
import static com.brainbooster.utils.TestEntities.gameResultBuilder;
import static org.assertj.core.api.Assertions.assertThat;

class GameResultMapperTest {

    private final GameResultMapper gameResultMapper = new GameResultMapper();

    @Test
    void shouldMapGameResultToDto() {
        LocalDateTime completedAt = LocalDateTime.of(2026, 1, 20, 12, 30);

        GameResult gameResult = gameResultBuilder()
                .resultId(10L)
                .mode(GameMode.WRITTEN)
                .score(7)
                .totalQuestions(10)
                .durationSeconds(90)
                .completedAt(completedAt)
                .build();

        GameResultDTO dto = gameResultMapper.toDto(gameResult);

        assertThat(dto).isEqualTo(createGameResultDTO(gameResult));
    }
}