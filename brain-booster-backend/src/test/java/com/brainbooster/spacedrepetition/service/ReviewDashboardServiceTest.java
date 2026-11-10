
package com.brainbooster.spacedrepetition.service;

import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.spacedrepetition.dto.ReviewDashboardDTO;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLogRepository;
import com.brainbooster.spacedrepetition.history.ReviewRating;
import com.brainbooster.spacedrepetition.settings.UserReviewSettings;
import com.brainbooster.spacedrepetition.settings.UserReviewSettingsRepository;
import com.brainbooster.spacedrepetition.state.FlashcardReviewStateRepository;
import com.brainbooster.spacedrepetition.state.ReviewStage;
import com.brainbooster.spacedrepetition.stats.ReviewActivityEvent;
import com.brainbooster.spacedrepetition.stats.ReviewStageCount;
import com.brainbooster.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * verifies dashboard aggregation, empty states and local-day boundaries
 */
@ExtendWith(MockitoExtension.class)
class ReviewDashboardServiceTest {

    private static final Long USER_ID = 1L;

    private static final Instant CURRENT_TIME = Instant.parse("2026-11-09T12:00:00Z");

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private FlashcardReviewStateRepository reviewStateRepository;

    @Mock
    private FlashcardReviewLogRepository reviewLogRepository;

    @Mock
    private UserReviewSettingsRepository reviewSettingsRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReviewDashboardService reviewDashboardService;

    @BeforeEach
    void setUp() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new AuthenticatedUser(USER_ID, Role.USER));
    }

    @Test
    void getDashboard_shouldReturnEmptyStatistics_whenUserHasNoReviews() {
        // given
        when(clock.instant()).thenReturn(CURRENT_TIME);

        when(reviewSettingsRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // when
        ReviewDashboardDTO dashboard = reviewDashboardService.getDashboard();

        // then
        assertThat(dashboard.totalCards()).isZero();
        assertThat(dashboard.newCards()).isZero();
        assertThat(dashboard.learningCards()).isZero();
        assertThat(dashboard.reviewCards()).isZero();
        assertThat(dashboard.relearningCards()).isZero();
        assertThat(dashboard.dueNow()).isZero();
        assertThat(dashboard.reviewedToday()).isZero();
        assertThat(dashboard.timezone()).isEqualTo("UTC");

        assertThat(dashboard.activity()).hasSize(14);
        assertThat(dashboard.ratings()).hasSize(4);
        assertThat(dashboard.forecast()).hasSize(7);

        assertThat(dashboard.activity())
                .allSatisfy(day -> {
                    assertThat(day.reviews()).isZero();
                    assertThat(day.newCards()).isZero();
                });

        assertThat(dashboard.ratings())
                .allSatisfy(rating -> assertThat(rating.count()).isZero());

        assertThat(dashboard.forecast())
                .allSatisfy(day -> assertThat(day.dueCards()).isZero());
    }

    @Test
    void getDashboard_shouldAggregateStatesActivityAndForecast() {
        // given
        when(clock.instant()).thenReturn(CURRENT_TIME);

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.of(
                        UserReviewSettings.builder()
                                .timezone("Europe/Warsaw")
                                .build()));

        when(flashcardRepository.countOwnedFlashcards(USER_ID))
                .thenReturn(6L);

        when(reviewStateRepository.countStagesForUser(USER_ID))
                .thenReturn(List.of(
                        new ReviewStageCount(ReviewStage.NEW, 1),
                        new ReviewStageCount(ReviewStage.LEARNING, 2),
                        new ReviewStageCount(ReviewStage.REVIEW, 1),
                        new ReviewStageCount(ReviewStage.RELEARNING, 1)));

        when(reviewStateRepository.countDueStates(
                eq(USER_ID),
                any(),
                eq(CURRENT_TIME)
        )).thenReturn(2L);

        when(reviewLogRepository.findActivityEvents(
                eq(USER_ID),
                any(Instant.class),
                eq(CURRENT_TIME)
        )).thenReturn(List.of(
                new ReviewActivityEvent(
                        Instant.parse("2026-11-08T21:00:00Z"),
                        ReviewRating.AGAIN,
                        ReviewStage.REVIEW),

                new ReviewActivityEvent(
                        Instant.parse("2026-11-09T09:00:00Z"),
                        ReviewRating.GOOD,
                        ReviewStage.NEW),

                new ReviewActivityEvent(Instant.parse("2026-11-09T10:00:00Z"),
                        ReviewRating.EASY,
                        ReviewStage.REVIEW)));

        when(reviewStateRepository.findFutureDueDates(
                eq(USER_ID),
                any(),
                eq(CURRENT_TIME),
                any(Instant.class)
        )).thenReturn(List.of(
                Instant.parse("2026-11-09T18:00:00Z"),
                Instant.parse("2026-11-10T08:00:00Z")));

        // when
        ReviewDashboardDTO dashboard = reviewDashboardService.getDashboard();

        // then
        assertThat(dashboard.totalCards()).isEqualTo(6);
        assertThat(dashboard.newCards()).isEqualTo(2);
        assertThat(dashboard.learningCards()).isEqualTo(2);
        assertThat(dashboard.reviewCards()).isEqualTo(1);
        assertThat(dashboard.relearningCards()).isEqualTo(1);
        assertThat(dashboard.dueNow()).isEqualTo(2);
        assertThat(dashboard.reviewedToday()).isEqualTo(2);

        assertThat(dashboard.activity().get(12)).isEqualTo(
                new ReviewDashboardDTO.DailyActivity(
                        LocalDate.of(2026, Month.NOVEMBER, 8),
                        1,
                        0));

        assertThat(dashboard.activity().get(13)).isEqualTo(
                new ReviewDashboardDTO.DailyActivity(
                        LocalDate.of(2026, Month.NOVEMBER, 9),
                        2,
                        1));

        assertThat(dashboard.ratings()).containsExactly(
                new ReviewDashboardDTO.RatingCount(ReviewRating.AGAIN, 1),
                new ReviewDashboardDTO.RatingCount(ReviewRating.HARD, 0),
                new ReviewDashboardDTO.RatingCount(ReviewRating.GOOD, 1),
                new ReviewDashboardDTO.RatingCount(ReviewRating.EASY, 1));

        assertThat(dashboard.forecast().get(0)).isEqualTo(
                new ReviewDashboardDTO.DailyForecast(
                        LocalDate.of(2026, Month.NOVEMBER, 9),
                        3));

        assertThat(dashboard.forecast().get(1)).isEqualTo(
                new ReviewDashboardDTO.DailyForecast(
                        LocalDate.of(2026, Month.NOVEMBER, 10),
                        1));
    }

    @Test
    void getDashboard_shouldUseLocalDayBoundaries_whenDaylightSavingTimeEnds() {
        // given
        Instant currentTime = Instant.parse("2026-10-25T12:00:00Z");

        when(clock.instant()).thenReturn(currentTime);

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.of(
                        UserReviewSettings.builder()
                                .timezone("Europe/Warsaw")
                                .build()));

        // when
        reviewDashboardService.getDashboard();

        // then
        verify(reviewLogRepository).findActivityEvents(
                USER_ID,
                Instant.parse("2026-10-11T22:00:00Z"),
                currentTime);

        verify(reviewStateRepository).findFutureDueDates(
                eq(USER_ID),
                any(),
                eq(currentTime),
                eq(Instant.parse("2026-10-31T23:00:00Z")));
    }

    @Test
    void getDashboard_shouldCountUnstartedFlashcardsAsNew() {
        // given
        when(clock.instant()).thenReturn(CURRENT_TIME);

        when(reviewSettingsRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        when(flashcardRepository.countOwnedFlashcards(USER_ID))
                .thenReturn(10L);

        when(reviewStateRepository.countStagesForUser(USER_ID))
                .thenReturn(List.of(
                        new ReviewStageCount(ReviewStage.REVIEW, 3),
                        new ReviewStageCount(ReviewStage.LEARNING, 2)));

        // when
        ReviewDashboardDTO dashboard = reviewDashboardService.getDashboard();

        // then
        assertThat(dashboard.totalCards()).isEqualTo(10);
        assertThat(dashboard.newCards()).isEqualTo(5);
    }
}
