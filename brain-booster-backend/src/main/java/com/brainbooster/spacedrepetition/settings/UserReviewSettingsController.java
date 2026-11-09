package com.brainbooster.spacedrepetition.settings;

import com.brainbooster.spacedrepetition.dto.UpdateUserReviewSettingsRequest;
import com.brainbooster.spacedrepetition.dto.UserReviewSettingsDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes authenticated endpoints for managing FSRS review preferences.
 *
 * <p>User identity is derived from the JWT security context.
 * Clients cannot access or update another user's settings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews/settings")
public class UserReviewSettingsController {

    private final UserReviewSettingsService reviewSettingsService;

    /**
     * Returns the authenticated user's effective review settings.
     *
     * @return current or default settings
     */
    @GetMapping
    public UserReviewSettingsDTO getSettings() {
        return reviewSettingsService.getSettings();
    }

    /**
     * Applies a partial update to the authenticated user's settings.
     *
     * @param request validated settings update
     * @return updated settings
     */
    @PatchMapping
    public UserReviewSettingsDTO updateSettings(
            @Valid @RequestBody UpdateUserReviewSettingsRequest request
    ) {
        return reviewSettingsService.updateSettings(request);
    }
}
