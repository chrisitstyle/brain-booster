package com.brainbooster.gameresult;

import com.brainbooster.gameresult.dto.GameResultDTO;
import com.brainbooster.gameresult.dto.SaveGameResultRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Game Results",
        description = "Endpoints for saving and retrieving latest game results for each user, study set, and game mode."
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/game-results")
public class GameResultController {

    private final GameResultService gameResultService;

    @Operation(
            summary = "Save or update a game result",
            description = """
                    Saves the latest result for the authenticated user.
                    If a result already exists for the same user, study set, and game mode,
                    it will be updated instead of creating a duplicate record.
                    """
    )
    @ApiResponse(
            responseCode = "200", description = "Game result saved or updated successfully.",
            content = @Content(schema = @Schema(implementation = GameResultDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid request body.", content = @Content)
    @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Study set not found.", content = @Content)
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public GameResultDTO saveGameResult(
            @Valid @RequestBody SaveGameResultRequest request
    ) {
        return gameResultService.saveGameResult(request);
    }

    @Operation(
            summary = "Get my game results",
            description = """
                    Returns game results for the currently authenticated user.
                    If setId is provided, only results for that study set are returned.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Authenticated user's game results returned successfully.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GameResultDTO.class)))
    )
    @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content)
    @GetMapping("/me")
    public List<GameResultDTO> getMyGameResults(
            @Parameter(description = "Optional study set ID used to filter results.", example = "12")
            @RequestParam(required = false) Long setId
    ) {
        return gameResultService.getMyGameResults(setId);
    }

    @Operation(
            summary = "Get all game results",
            description = """
                    Returns all game results in the system.
                    This endpoint is available only for administrators.
                    If setId is provided, only results for that study set are returned.
                    """
    )
    @ApiResponse(responseCode = "200", description = "All game results returned successfully.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GameResultDTO.class)))
    )
    @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Only administrators can access this endpoint.", content = @Content)
    @GetMapping
    public List<GameResultDTO> getAllGameResults(
            @Parameter(description = "Optional study set ID used to filter results.", example = "12")
            @RequestParam(required = false) Long setId
    ) {
        return gameResultService.getAllGameResults(setId);
    }

    @Operation(
            summary = "Get a game result by ID",
            description = """
                    Returns a single game result by ID.
                    The result can be accessed by its owner or by an administrator.
                    """
    )

    @ApiResponse(responseCode = "200", description = "Game result returned successfully.",
            content = @Content(schema = @Schema(implementation = GameResultDTO.class))
    )
    @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content)
    @ApiResponse(responseCode = "403", description = "User is not allowed to access this result.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Game result not found.", content = @Content)
    @GetMapping("/{resultId}")
    public GameResultDTO getGameResultById(
            @Parameter(description = "ID of the game result.", example = "1")
            @PathVariable Long resultId
    ) {
        return gameResultService.getGameResultById(resultId);
    }

    @Operation(
            summary = "Delete a game result",
            description = """
                    Deletes a game result by ID.
                    The result can be deleted by its owner or by an administrator.
                    """
    )
    @ApiResponse(responseCode = "204", description = "Game result deleted successfully.", content = @Content)
    @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content)
    @ApiResponse(responseCode = "403", description = "User is not allowed to delete this result.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Game result not found.", content = @Content)
    @DeleteMapping("/{resultId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGameResult(
            @Parameter(description = "ID of the game result to delete.", example = "1")
            @PathVariable Long resultId
    ) {
        gameResultService.deleteGameResult(resultId);
    }
}