
package com.brainbooster.spacedrepetition.controller;

import com.brainbooster.spacedrepetition.dto.ReviewCardDTO;
import com.brainbooster.spacedrepetition.dto.ReviewResultDTO;
import com.brainbooster.spacedrepetition.dto.SubmitReviewRequest;
import com.brainbooster.spacedrepetition.service.ReviewSchedulingService;
import com.brainbooster.spacedrepetition.service.ReviewSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes authenticated spaced repetition operations.
 *
 * <p>Users may access only their own flashcard sets.
 * Review submissions use request IDs to support safe retries.
 */
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(
        name = "Spaced Repetition",
        description = "FSRS flashcard review and scheduling endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SpacedRepetitionController {

    private final ReviewSchedulingService reviewSchedulingService;
    private final ReviewSessionService reviewSessionService;

    /**
     * Returns new and due flashcards for a specific set.
     */
    @GetMapping("/sets/{setId}/queue")
    @Operation(summary = "Get flashcards available for review")
    public List<ReviewCardDTO> getReviewQueue(@PathVariable Long setId) {
        return reviewSessionService.getReviewQueue(setId);
    }

    /**
     * Records a flashcard rating and schedules its next review.
     *
     * <p>Identical retries with the same request ID return
     * the original review result.
     */
    @PostMapping
    @Operation(summary = "Submit a flashcard review")
    public ReviewResultDTO submitReview(@Valid @RequestBody SubmitReviewRequest request) {
        return reviewSchedulingService.submitReview(request);
    }
}
