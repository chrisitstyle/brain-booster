package com.brainbooster.spacedrepetition.controller;

import com.brainbooster.spacedrepetition.dto.ReviewDashboardDTO;
import com.brainbooster.spacedrepetition.service.ReviewDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes spaced repetition dashboard statistics
 * for the currently authenticated user.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews/dashboard")
public class ReviewDashboardController {

    private final ReviewDashboardService reviewDashboardService;

    /**
     * Returns aggregated FSRS learning statistics.
     *
     * @return current user's spaced repetition dashboard
     */
    @GetMapping
    public ReviewDashboardDTO getDashboard() {
        return reviewDashboardService.getDashboard();
    }
}