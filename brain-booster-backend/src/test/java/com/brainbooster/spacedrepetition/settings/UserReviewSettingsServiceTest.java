
package com.brainbooster.spacedrepetition.settings;

import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.spacedrepetition.dto.UpdateUserReviewSettingsRequest;
import com.brainbooster.spacedrepetition.dto.UserReviewSettingsDTO;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Verifies retrieval, updates and validation of user review settings.
 *
 * <p>Repository dependencies are mocked so the tests focus on
 * settings-related business rules.
 */
@ExtendWith(MockitoExtension.class)
class UserReviewSettingsServiceTest {

    private static final Long USER_ID = 2L;

    @Mock
    private UserReviewSettingsRepository reviewSettingsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private UserReviewSettingsService reviewSettingsService;

    @Test
    void getSettings_shouldReturnDefaults_whenUserHasNoSavedSettings() {
        // given
        authenticateUser();

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        // when
        UserReviewSettingsDTO actualSettings = reviewSettingsService.getSettings();

        // then
        assertThat(actualSettings).isEqualTo(
                new UserReviewSettingsDTO(
                        0.90,
                        20,
                        200,
                        "UTC"));

        verify(reviewSettingsRepository, never())
                .saveAndFlush(any(UserReviewSettings.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateSettings_shouldCreateSettings_whenUserHasNoSavedSettings() {
        // given
        authenticateUser();

        User user = User.builder()
                .userId(USER_ID)
                .build();

        UpdateUserReviewSettingsRequest updateRequest = new UpdateUserReviewSettingsRequest(
                        0.92,
                        15,
                        150,
                        "Europe/Warsaw");

        when(userRepository.findByIdForReviewUpdate(USER_ID))
                .thenReturn(Optional.of(user));

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        when(reviewSettingsRepository.saveAndFlush(
                any(UserReviewSettings.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UserReviewSettingsDTO actualSettings = reviewSettingsService.updateSettings(updateRequest);

        // then
        assertThat(actualSettings).isEqualTo(
                new UserReviewSettingsDTO(
                        0.92,
                        15,
                        150,
                        "Europe/Warsaw"));

        verify(reviewSettingsRepository).saveAndFlush(
                argThat(settings -> settings.getUser() == user
                                && settings.getDesiredRetention() == 0.92
                                && settings.getNewCardsPerDay() == 15
                                && settings.getReviewLimitPerDay() == 150
                                && "Europe/Warsaw".equals(
                                settings.getTimezone())));
    }

    @Test
    void updateSettings_shouldPreserveExistingValues_whenPatchIsPartial() {
        // given
        authenticateUser();

        User user = User.builder()
                .userId(USER_ID)
                .build();

        UserReviewSettings existingSettings = UserReviewSettings.builder()
                        .user(user)
                        .desiredRetention(0.93)
                        .newCardsPerDay(10)
                        .reviewLimitPerDay(120)
                        .timezone("UTC")
                        .build();

        UpdateUserReviewSettingsRequest updateRequest = new UpdateUserReviewSettingsRequest(
                        null,
                        30,
                        null,
                        null);

        when(userRepository.findByIdForReviewUpdate(USER_ID)).thenReturn(Optional.of(user));

        when(reviewSettingsRepository.findById(USER_ID)).thenReturn(Optional.of(existingSettings));

        when(reviewSettingsRepository.saveAndFlush(
                any(UserReviewSettings.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UserReviewSettingsDTO actualSettings = reviewSettingsService.updateSettings(updateRequest);

        // then
        assertThat(actualSettings).isEqualTo(
                new UserReviewSettingsDTO(
                        0.93,
                        30,
                        120,
                        "UTC"));
    }

    @Test
    void updateSettings_shouldUpdateTimezone_whenValidIanaTimezoneIsProvided() {
        // given
        authenticateUser();

        User user = User.builder()
                .userId(USER_ID)
                .build();

        UserReviewSettings existingSettings = UserReviewSettings.builder()
                        .user(user)
                        .build();

        UpdateUserReviewSettingsRequest updateRequest = new UpdateUserReviewSettingsRequest(
                        null,
                        null,
                        null,
                        "  Europe/Warsaw  ");

        when(userRepository.findByIdForReviewUpdate(USER_ID))
                .thenReturn(Optional.of(user));

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.of(existingSettings));

        when(reviewSettingsRepository.saveAndFlush(
                any(UserReviewSettings.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UserReviewSettingsDTO actualSettings = reviewSettingsService.updateSettings(updateRequest);

        // then
        assertThat(actualSettings.timezone()).isEqualTo("Europe/Warsaw");

        assertThat(actualSettings.desiredRetention()).isEqualTo(0.90);

        assertThat(actualSettings.newCardsPerDay()).isEqualTo(20);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.69, 0.98, 1.0})
    void updateSettings_shouldRejectRetention_whenOutsideAllowedRange(
            double desiredRetention
    ) {
        // given
        UpdateUserReviewSettingsRequest updateRequest = new UpdateUserReviewSettingsRequest(
                        desiredRetention,
                        null,
                        null,
                        null);

        // when
        // then
        assertThatThrownBy(() ->
                reviewSettingsService.updateSettings(updateRequest)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Desired retention must be between 0.70 and 0.97.");

        verifyNoInteractions(userRepository, reviewSettingsRepository);
    }

    @Test
    void updateSettings_shouldRejectRetention_whenValueIsNotFinite() {
        // given
        UpdateUserReviewSettingsRequest updateRequest = new UpdateUserReviewSettingsRequest(
                        Double.NaN,
                        null,
                        null,
                        null);

        // when
        // then
        assertThatThrownBy(() -> reviewSettingsService.updateSettings(updateRequest)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Desired retention must be between 0.70 and 0.97.");
    }

    @Test
    void updateSettings_shouldRejectTimezone_whenIdentifierIsInvalid() {
        // given
        UpdateUserReviewSettingsRequest updateRequest = new UpdateUserReviewSettingsRequest(
                        null,
                        null,
                        null,
                        "Europe/InvalidCity");

        // when
        // then
        assertThatThrownBy(() -> reviewSettingsService.updateSettings(updateRequest)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid IANA time zone.");

        verifyNoInteractions(userRepository, reviewSettingsRepository);
    }

    @Test
    void updateSettings_shouldRejectDailyLimit_whenValueIsNegative() {
        // given
        UpdateUserReviewSettingsRequest updateRequest = new UpdateUserReviewSettingsRequest(
                        null,
                        -1,
                        null,
                        null);

        // when
        // then
        assertThatThrownBy(() -> reviewSettingsService.updateSettings(updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New cards per day must be between 0 and 10000.");

        verifyNoInteractions(userRepository, reviewSettingsRepository);
    }

    @Test
    void updateSettings_shouldRejectRequest_whenNoFieldsAreProvided() {
        // given
        UpdateUserReviewSettingsRequest updateRequest = new UpdateUserReviewSettingsRequest(
                        null,
                        null,
                        null,
                        null);

        // when
        // then
        assertThatThrownBy(() -> reviewSettingsService.updateSettings(updateRequest)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one review setting must be provided.");

        verifyNoInteractions(userRepository, reviewSettingsRepository);
    }

    private void authenticateUser() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new AuthenticatedUser(
                        USER_ID,
                        Role.USER));
    }
}
