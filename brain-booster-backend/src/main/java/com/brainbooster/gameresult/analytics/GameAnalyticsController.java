package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.WeakFlashcardDTO;
import com.brainbooster.gameresult.analytics.dto.GameAnalyticsSummaryDTO;
import com.brainbooster.gameresult.analytics.dto.GameProgressPointDTO;
import com.brainbooster.gameresult.analytics.dto.QuestionTypeAnalyticsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/game-analytics")
@RequiredArgsConstructor
@Tag(name = "Game Analytics", description = "Endpoints for game progress and learning analytics")
@SecurityRequirement(name = "bearerAuth")
public class GameAnalyticsController {

    private final GameAnalyticsService gameAnalyticsService;

    @GetMapping("/me/sets/{setId}/summary")
    @Operation(
            summary = "Get summary analytics for a flashcard set",
            description = "Retrieves a summary of game analytics (total games, accuracy, etc.) for a specific flashcard set belonging to the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Summary analytics retrieved successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "404", description = "Flashcard set not found")
    public GameAnalyticsSummaryDTO getMySetSummary(
            @Parameter(description = "ID of the flashcard set", required = true)
            @PathVariable Long setId
    ) {
        return gameAnalyticsService.getMySetSummary(setId);
    }

    @GetMapping("/me/sets/{setId}/progress")
    @Operation(
            summary = "Get progress data for a flashcard set",
            description = "Retrieves progress data points over time for a specific flashcard set belonging to the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Progress data retrieved successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "404", description = "Flashcard set not found")
    public List<GameProgressPointDTO> getMySetProgress(
            @Parameter(description = "ID of the flashcard set", required = true)
            @PathVariable Long setId
    ) {
        return gameAnalyticsService.getMySetProgress(setId);
    }

    @GetMapping("/me/sets/{setId}/weak-flashcards")
    @Operation(
            summary = "Get weak flashcards for a flashcard set",
            description = "Retrieves a list of flashcards with the lowest accuracy from a specific flashcard set to help the user focus on weak points."
    )
    @ApiResponse(responseCode = "200", description = "Weak flashcards retrieved successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "404", description = "Flashcard set not found")
    public List<WeakFlashcardDTO> getMySetWeakFlashcards(
            @Parameter(description = "ID of the flashcard set", required = true)
            @PathVariable Long setId
    ) {
        return gameAnalyticsService.getMySetWeakFlashcards(setId);
    }

    @GetMapping("/me/sets/{setId}/question-types")
    @Operation(
            summary = "Get question type analytics for a flashcard set",
            description = "Retrieves analytics broken down by the type of question (e.g., matching, typing, quiz) for a specific flashcard set."
    )
    @ApiResponse(responseCode = "200", description = "Question type analytics retrieved successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "404", description = "Flashcard set not found")
    public List<QuestionTypeAnalyticsDTO> getMySetQuestionTypeAnalytics(
            @Parameter(description = "ID of the flashcard set", required = true)
            @PathVariable Long setId
    ) {
        return gameAnalyticsService.getMySetQuestionTypeAnalytics(setId);
    }
}