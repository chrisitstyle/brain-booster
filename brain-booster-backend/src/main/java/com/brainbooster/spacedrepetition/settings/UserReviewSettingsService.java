
package com.brainbooster.spacedrepetition.settings;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.spacedrepetition.dto.UpdateUserReviewSettingsRequest;
import com.brainbooster.spacedrepetition.dto.UserReviewSettingsDTO;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Set;

/**
 * Manages spaced repetition preferences for the authenticated user.
 *
 * <p>Reading preferences does not create database records.
 * Default values are returned when a user has not saved any settings.
 *
 * <p>Updates lock the user row before creating or modifying settings,
 * preventing concurrent requests from creating duplicate records.
 */
@Service
@RequiredArgsConstructor
public class UserReviewSettingsService {

    private static final double MIN_DESIRED_RETENTION = 0.70;
    private static final double MAX_DESIRED_RETENTION = 0.97;
    private static final int MAX_DAILY_LIMIT = 10000;

    private static final Set<String> SUPPORTED_TIMEZONES = Set.copyOf(ZoneId.getAvailableZoneIds());

    private final UserReviewSettingsRepository reviewSettingsRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Returns the current user's settings or their default values.
     *
     * @return effective spaced repetition preferences
     */
    @Transactional(readOnly = true)
    public UserReviewSettingsDTO getSettings() {
        Long userId = currentUserProvider.getCurrentUser().userId();

        return reviewSettingsRepository.findById(userId)
                .map(this::mapToDTO)
                .orElseGet(() -> mapToDTO(
                        UserReviewSettings.builder().build()));
    }

    /**
     * Updates only the supplied settings for the authenticated user.
     *
     * @param request requested partial settings update
     * @return updated spaced repetition preferences
     */
    @Transactional
    public UserReviewSettingsDTO updateSettings(
            UpdateUserReviewSettingsRequest request
    ) {
        validateRequest(request);

        Long userId = currentUserProvider.getCurrentUser().userId();

        // serialize settings updates and review submissions per user
        User user = userRepository.findByIdForReviewUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        UserReviewSettings settings = reviewSettingsRepository
                .findById(userId)
                .orElseGet(() -> UserReviewSettings.builder()
                                .user(user)
                                .build());

        if (request.desiredRetention() != null) {
            settings.setDesiredRetention(request.desiredRetention());
        }

        if (request.newCardsPerDay() != null) {
            settings.setNewCardsPerDay(request.newCardsPerDay());
        }

        if (request.reviewLimitPerDay() != null) {
            settings.setReviewLimitPerDay(request.reviewLimitPerDay());
        }

        if (request.timezone() != null) {
            settings.setTimezone(request.timezone().trim());
        }

        UserReviewSettings savedSettings = reviewSettingsRepository.saveAndFlush(settings);

        return mapToDTO(savedSettings);
    }

    private void validateRequest(
            UpdateUserReviewSettingsRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Review settings request must not be null.");
        }

        if (request.desiredRetention() == null
                && request.newCardsPerDay() == null
                && request.reviewLimitPerDay() == null
                && request.timezone() == null) {
            throw new IllegalArgumentException("At least one review setting must be provided.");
        }

        Double desiredRetention = request.desiredRetention();

        if (desiredRetention != null
                && (!Double.isFinite(desiredRetention)
                || desiredRetention < MIN_DESIRED_RETENTION
                || desiredRetention > MAX_DESIRED_RETENTION)) {
            throw new IllegalArgumentException("Desired retention must be between 0.70 and 0.97.");
        }

        validateDailyLimit(request.newCardsPerDay(),
                "New cards per day");

        validateDailyLimit(request.reviewLimitPerDay(),
                "Review limit per day");

        if (request.timezone() != null) {
            String timezone = request.timezone().trim();

            if (!SUPPORTED_TIMEZONES.contains(timezone)) {
                throw new IllegalArgumentException("Invalid IANA time zone.");
            }
        }
    }

    private void validateDailyLimit(
            Integer dailyLimit,
            String settingName
    ) {
        if (dailyLimit != null
                && (dailyLimit < 0 || dailyLimit > MAX_DAILY_LIMIT)) {
            throw new IllegalArgumentException(settingName + " must be between 0 and 10000.");
        }
    }

    private UserReviewSettingsDTO mapToDTO(
            UserReviewSettings settings
    ) {
        return new UserReviewSettingsDTO(
                settings.getDesiredRetention(),
                settings.getNewCardsPerDay(),
                settings.getReviewLimitPerDay(),
                settings.getTimezone());
    }
}
