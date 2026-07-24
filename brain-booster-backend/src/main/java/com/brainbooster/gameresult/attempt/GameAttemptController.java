package com.brainbooster.gameresult.attempt;

import com.brainbooster.gameresult.dto.GameAttemptDTO;
import com.brainbooster.gameresult.dto.GameAttemptSummaryDTO;
import com.brainbooster.gameresult.dto.GameQuestionResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/game-attempts")
@RequiredArgsConstructor
@Tag(name = "Game Attempts", description = "Endpoints for reading game attempt history")
@SecurityRequirement(name = "bearerAuth")
public class GameAttemptController {

    private final GameAttemptService gameAttemptService;

    @GetMapping("/me")
    @Operation(
            summary = "Get current user's game attempt history",
            description = "Retrieves a paginated list of all game attempts completed by the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Game attempts retrieved successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    public ResponseEntity<Page<GameAttemptSummaryDTO>> getMyGameAttempts(
            @Parameter(description = "Optional flashcard set ID filter")
            @RequestParam(required = false) Long setId,

            @Parameter(description = "Optional game mode filter (e.g., multiple-choice, written, matching, custom-test)")
            @RequestParam(required = false) String mode,

            @Parameter(description = "Optional start date filter in ISO format (e.g., 2026-01-01)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Optional end date filter in ISO format (e.g., 2026-01-31)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @ParameterObject
            @PageableDefault(size = 20, sort = "completedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                gameAttemptService.getMyGameAttempts(setId, mode, from, to, pageable)
        );
    }

    @GetMapping("/me/sets/{setId}")
    @Operation(
            summary = "Get current user's game attempt history for a specific flashcard set",
            description = "Retrieves a paginated list of game attempts for a specific flashcard set belonging to the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Game attempts retrieved successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "404", description = "Flashcard set not found")
    public ResponseEntity<Page<GameAttemptSummaryDTO>> getMyGameAttemptsBySetId(
            @Parameter(description = "ID of the flashcard set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "Optional game mode filter (e.g., multiple-choice, written, matching, custom-test)")
            @RequestParam(required = false) String mode,

            @Parameter(description = "Optional start date filter in ISO format (e.g., 2026-01-01)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Optional end date filter in ISO format (e.g., 2026-01-31)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @ParameterObject
            @PageableDefault(size = 20, sort = "completedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                gameAttemptService.getMyGameAttemptsBySetId(setId, mode, from, to, pageable)
        );
    }

    @GetMapping("/{attemptId}")
    @Operation(
            summary = "Get details of a single game attempt",
            description = "Retrieves full details (settings, score, duration) of a specific game attempt by its ID."
    )
    @ApiResponse(responseCode = "200", description = "Game attempt details retrieved successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User is not authorized to access this attempt")
    @ApiResponse(responseCode = "404", description = "Game attempt not found")
    public ResponseEntity<GameAttemptDTO> getGameAttemptById(
            @Parameter(description = "ID of the game attempt", required = true)
            @PathVariable Long attemptId
    ) {
        return ResponseEntity.ok(
                gameAttemptService.getGameAttemptById(attemptId)
        );
    }

    @GetMapping("/{attemptId}/question-results")
    @Operation(
            summary = "Get question-level results for a single game attempt",
            description = "Retrieves a detailed list of all questions answered (correct and incorrect) during a specific game attempt."
    )
    @ApiResponse(responseCode = "200", description = "Question results retrieved successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User is not authorized to access this attempt")
    @ApiResponse(responseCode = "404", description = "Game attempt not found")
    public ResponseEntity<List<GameQuestionResultDTO>> getQuestionResultsByAttemptId(
            @Parameter(description = "ID of the game attempt", required = true)
            @PathVariable Long attemptId
    ) {
        return ResponseEntity.ok(
                gameAttemptService.getQuestionResultsByAttemptId(attemptId)
        );
    }
}