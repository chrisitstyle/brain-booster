package com.brainbooster.gameresult.analytics;

import com.brainbooster.gameresult.WeakFlashcardDTO;
import com.brainbooster.gameresult.analytics.dto.GameAnalyticsSummaryDTO;
import com.brainbooster.gameresult.analytics.dto.GameProgressPointDTO;
import com.brainbooster.gameresult.analytics.dto.QuestionTypeAnalyticsDTO;
import io.swagger.v3.oas.annotations.Operation;
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
public class GameAnalyticsController {

    private final GameAnalyticsService gameAnalyticsService;

    @GetMapping("/me/sets/{setId}/summary")
    @Operation(summary = "Get summary analytics for a flashcard set")
    public GameAnalyticsSummaryDTO getMySetSummary(
            @PathVariable Long setId
    ) {
        return gameAnalyticsService.getMySetSummary(setId);
    }

    @GetMapping("/me/sets/{setId}/progress")
    @Operation(summary = "Get progress data for a flashcard set")
    public List<GameProgressPointDTO> getMySetProgress(
            @PathVariable Long setId
    ) {
        return gameAnalyticsService.getMySetProgress(setId);
    }

    @GetMapping("/me/sets/{setId}/weak-flashcards")
    @Operation(summary = "Get weak flashcards for a flashcard set")
    public List<WeakFlashcardDTO> getMySetWeakFlashcards(
            @PathVariable Long setId
    ) {
        return gameAnalyticsService.getMySetWeakFlashcards(setId);
    }

    @GetMapping("/me/sets/{setId}/question-types")
    @Operation(summary = "Get question type analytics for a flashcard set")
    public List<QuestionTypeAnalyticsDTO> getMySetQuestionTypeAnalytics(
            @PathVariable Long setId
    ) {
        return gameAnalyticsService.getMySetQuestionTypeAnalytics(setId);
    }
}