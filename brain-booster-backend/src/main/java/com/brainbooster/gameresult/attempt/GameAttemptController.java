package com.brainbooster.gameresult.attempt;

import com.brainbooster.gameresult.dto.GameAttemptDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/game-attempts")
@RequiredArgsConstructor
@Tag(name = "Game Attempts", description = "Endpoints for reading game attempt history")
public class GameAttemptController {

    private final GameAttemptService gameAttemptService;

    @GetMapping("/me")
    @Operation(summary = "Get current user's game attempt history")
    public ResponseEntity<List<GameAttemptDTO>> getMyGameAttempts() {
        return ResponseEntity.ok(gameAttemptService.getMyGameAttempts());
    }

    @GetMapping("/me/sets/{setId}")
    @Operation(summary = "Get current user's game attempt history for a specific flashcard set")
    public ResponseEntity<List<GameAttemptDTO>> getMyGameAttemptsBySetId(
            @PathVariable Long setId
    ) {
        return ResponseEntity.ok(gameAttemptService.getMyGameAttemptsBySetId(setId));
    }

    @GetMapping("/{attemptId}")
    @Operation(summary = "Get details of a single game attempt")
    public ResponseEntity<GameAttemptDTO> getGameAttemptById(
            @PathVariable Long attemptId
    ) {
        return ResponseEntity.ok(gameAttemptService.getGameAttemptById(attemptId));
    }
}
