package com.brainbooster.spacedrepetition.service;

import com.brainbooster.flashcard.FlashcardRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the authenticated user's spaced repetition dashboard.
 *
 * <p>Statistics are aggregated across all flashcard sets owned
 * by the user. Review activity and forecasts use the user's
 * configured local time zone.
 *
 * <p>The dashboard reports actual scheduled reviews without
 * applying daily review limits.
 */
@Service
@RequiredArgsConstructor
public class ReviewDashboardService {

    private static final int ACTIVITY_DAYS = 14;
    private static final int FORECAST_DAYS = 7;

    private static final List<ReviewStage> SCHEDULED_STAGES = List.of(
            ReviewStage.LEARNING,
            ReviewStage.REVIEW,
            ReviewStage.RELEARNING);

    private final FlashcardRepository flashcardRepository;
    private final FlashcardReviewStateRepository reviewStateRepository;
    private final FlashcardReviewLogRepository reviewLogRepository;
    private final UserReviewSettingsRepository reviewSettingsRepository;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    /**
     * Returns current learning statistics for the authenticated user.
     *
     * @return FSRS dashboard statistics
     */
    @Transactional(readOnly = true)
    public ReviewDashboardDTO getDashboard() {
        Long userId = currentUserProvider.getCurrentUser().userId();

        UserReviewSettings settings = reviewSettingsRepository
                .findById(userId)
                .orElseGet(() -> UserReviewSettings.builder().build());

        ZoneId timezone = ZoneId.of(settings.getTimezone());
        Instant currentTime = clock.instant();

        LocalDate today = currentTime
                .atZone(timezone)
                .toLocalDate();

        long totalCards = flashcardRepository.countOwnedFlashcards(userId);

        Map<ReviewStage, Long> stageCounts = getStageCounts(userId);

        long learningCards = stageCounts.get(ReviewStage.LEARNING);
        long reviewCards = stageCounts.get(ReviewStage.REVIEW);
        long relearningCards = stageCounts.get(ReviewStage.RELEARNING);

        // Cards without a persisted FSRS state are also NEW
        long newCards = Math.max(
                0L,
                totalCards - learningCards - reviewCards - relearningCards);

        long dueNow = reviewStateRepository.countDueStates(
                userId,
                SCHEDULED_STAGES,
                currentTime);

        Instant activityStart = today
                .minusDays(ACTIVITY_DAYS - 1L)
                .atStartOfDay(timezone)
                .toInstant();

        List<ReviewActivityEvent> reviewEvents = reviewLogRepository.findActivityEvents(
                        userId,
                        activityStart,
                        currentTime);

        Map<LocalDate, Long> dailyReviewCounts = new HashMap<>();
        Map<LocalDate, Long> dailyNewCardCounts = new HashMap<>();

        Map<ReviewRating, Long> ratingCounts = new EnumMap<>(ReviewRating.class);

        for (ReviewActivityEvent event : reviewEvents) {
            LocalDate reviewDate = event.reviewedAt()
                    .atZone(timezone)
                    .toLocalDate();

            dailyReviewCounts.merge(
                    reviewDate,
                    1L,
                    Long::sum);

            if (event.stageBefore() == ReviewStage.NEW) {
                dailyNewCardCounts.merge(
                        reviewDate,
                        1L,
                        Long::sum);
            }

            ratingCounts.merge(
                    event.rating(),
                    1L,
                    Long::sum);
        }

        List<ReviewDashboardDTO.DailyActivity> activity = buildActivity(today,
                        dailyReviewCounts,
                        dailyNewCardCounts);

        List<ReviewDashboardDTO.RatingCount> ratings = buildRatingDistribution(ratingCounts);

        List<ReviewDashboardDTO.DailyForecast> forecast = buildForecast(
                        userId,
                        today,
                        timezone,
                        currentTime,
                        dueNow);

        return new ReviewDashboardDTO(
                totalCards,
                newCards,
                learningCards,
                reviewCards,
                relearningCards,
                dueNow,
                dailyReviewCounts.getOrDefault(today, 0L),
                timezone.getId(),
                activity,
                ratings,
                forecast);
    }

    private Map<ReviewStage, Long> getStageCounts(Long userId) {
        Map<ReviewStage, Long> counts = new EnumMap<>(ReviewStage.class);

        for (ReviewStage stage : ReviewStage.values()) {
            counts.put(stage, 0L);
        }

        for (ReviewStageCount stageCount :
                reviewStateRepository.countStagesForUser(userId)) {
            counts.put(stageCount.stage(), stageCount.count());
        }

        return counts;
    }

    private List<ReviewDashboardDTO.DailyActivity> buildActivity(
            LocalDate today,
            Map<LocalDate, Long> dailyReviewCounts,
            Map<LocalDate, Long> dailyNewCardCounts
    ) {
        List<ReviewDashboardDTO.DailyActivity> activity = new ArrayList<>(ACTIVITY_DAYS);

        for (int day = ACTIVITY_DAYS - 1; day >= 0; day--) {
            LocalDate date = today.minusDays(day);

            activity.add(
                    new ReviewDashboardDTO.DailyActivity(
                            date,
                            dailyReviewCounts.getOrDefault(date, 0L),
                            dailyNewCardCounts.getOrDefault(date, 0L)));
        }

        return activity;
    }

    private List<ReviewDashboardDTO.RatingCount>
    buildRatingDistribution(
            Map<ReviewRating, Long> ratingCounts
    ) {
        List<ReviewDashboardDTO.RatingCount> distribution = new ArrayList<>();

        for (ReviewRating rating : ReviewRating.values()) {
            distribution.add(
                    new ReviewDashboardDTO.RatingCount(
                            rating,
                            ratingCounts.getOrDefault(rating, 0L)));
        }

        return distribution;
    }

    private List<ReviewDashboardDTO.DailyForecast> buildForecast(
            Long userId,
            LocalDate today,
            ZoneId timezone,
            Instant currentTime,
            long dueNow
    ) {
        Map<LocalDate, Long> dueCounts = new LinkedHashMap<>();

        for (int day = 0; day < FORECAST_DAYS; day++) {
            dueCounts.put(today.plusDays(day), 0L);
        }

        // iverdue and currently due cards are counted under today
        dueCounts.put(today, dueNow);

        LocalDate dayAfterForecast = today.plusDays(FORECAST_DAYS);

        Instant forecastEnd = dayAfterForecast
                .atStartOfDay(timezone)
                .toInstant();

        List<Instant> futureDueDates = reviewStateRepository.findFutureDueDates(
                        userId,
                        SCHEDULED_STAGES,
                        currentTime,
                        forecastEnd);

        for (Instant dueDate : futureDueDates) {
            LocalDate dueDay = dueDate
                    .atZone(timezone)
                    .toLocalDate();

            dueCounts.merge(dueDay, 1L, Long::sum);
        }

        List<ReviewDashboardDTO.DailyForecast> forecast = new ArrayList<>(FORECAST_DAYS);

        for (Map.Entry<LocalDate, Long> entry : dueCounts.entrySet()) {
            forecast.add(new ReviewDashboardDTO.DailyForecast(
                            entry.getKey(),
                            entry.getValue()));
        }

        return forecast;
    }
}
