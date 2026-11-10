package com.brainbooster.spacedrepetition;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.integration.AbstractRepositoryTest;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLog;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLogRepository;
import com.brainbooster.spacedrepetition.history.ReviewRating;
import com.brainbooster.spacedrepetition.state.FlashcardReviewState;
import com.brainbooster.spacedrepetition.state.FlashcardReviewStateRepository;
import com.brainbooster.spacedrepetition.state.ReviewStage;
import com.brainbooster.spacedrepetition.stats.ReviewActivityEvent;
import com.brainbooster.spacedrepetition.stats.ReviewStageCount;
import com.brainbooster.user.User;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies dashboard queries against PostgreSQL and ensures that
 * analytics never include another user's flashcards or reviews.
 */
class ReviewDashboardRepositoryIntegrationTest extends AbstractRepositoryTest {

    private static final Instant CURRENT_TIME = Instant.parse("2026-11-09T12:00:00Z");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private FlashcardReviewStateRepository reviewStateRepository;

    @Autowired
    private FlashcardReviewLogRepository reviewLogRepository;

    private Long firstUserId;
    private Long secondUserId;

    @BeforeEach
    void setUp() {
        User firstUser = persistUser("dashboard_first_user");
        User secondUser = persistUser("dashboard_second_user");

        firstUserId = firstUser.getUserId();
        secondUserId = secondUser.getUserId();

        FlashcardSet firstSet = persistSet(firstUser, "First user's flashcards");

        FlashcardSet secondSet = persistSet(secondUser, "Second user's flashcards");

        Flashcard firstCard = persistFlashcard(firstSet, "Java");

        Flashcard secondCard = persistFlashcard(firstSet, "Spring");

        Flashcard otherUserCard = persistFlashcard(secondSet, "PostgreSQL");

        FlashcardReviewState firstState = persistState(
                        firstUser,
                        firstCard,
                        ReviewStage.REVIEW,
                        CURRENT_TIME.minusSeconds(60));

        persistState(
                firstUser,
                secondCard,
                ReviewStage.LEARNING,
                CURRENT_TIME.plusSeconds(3600));

        FlashcardReviewState otherUserState = persistState(
                        secondUser,
                        otherUserCard,
                        ReviewStage.REVIEW,
                        CURRENT_TIME.minusSeconds(60));

        persistLog(
                firstState,
                ReviewRating.GOOD,
                ReviewStage.NEW,
                CURRENT_TIME.minusSeconds(120));

        persistLog(
                otherUserState,
                ReviewRating.EASY,
                ReviewStage.REVIEW,
                CURRENT_TIME.minusSeconds(90));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void dashboardQueries_shouldCountOnlyAuthenticatedUsersCards() {
        // given
        List<ReviewStage> scheduledStages = List.of(
                ReviewStage.LEARNING,
                ReviewStage.REVIEW,
                ReviewStage.RELEARNING);

        // when
        long ownedCards = flashcardRepository.countOwnedFlashcards(firstUserId);

        List<ReviewStageCount> stageCounts = reviewStateRepository.countStagesForUser(firstUserId);

        long dueNow = reviewStateRepository.countDueStates(
                firstUserId,
                scheduledStages,
                CURRENT_TIME);

        List<Instant> futureDueDates = reviewStateRepository.findFutureDueDates(
                        firstUserId,
                        scheduledStages,
                        CURRENT_TIME,
                        CURRENT_TIME.plusSeconds(86400));

        List<ReviewActivityEvent> activity = reviewLogRepository.findActivityEvents(
                        firstUserId,
                        CURRENT_TIME.minusSeconds(86400),
                        CURRENT_TIME);

        // then
        assertThat(ownedCards).isEqualTo(2);

        assertThat(stageCounts).containsExactlyInAnyOrder(
                new ReviewStageCount(ReviewStage.REVIEW, 1),
                new ReviewStageCount(ReviewStage.LEARNING, 1));

        assertThat(dueNow).isEqualTo(1);

        assertThat(futureDueDates).containsExactly(CURRENT_TIME.plusSeconds(3600));

        assertThat(activity).containsExactly(
                new ReviewActivityEvent(
                        CURRENT_TIME.minusSeconds(120),
                        ReviewRating.GOOD,
                        ReviewStage.NEW));
    }

    @Test
    void dashboardQueries_shouldKeepDifferentUsersStatisticsSeparate() {
        // given
        List<ReviewStage> scheduledStages = List.of(
                ReviewStage.LEARNING,
                ReviewStage.REVIEW,
                ReviewStage.RELEARNING);

        // when
        long firstUserDue = reviewStateRepository.countDueStates(
                firstUserId,
                scheduledStages,
                CURRENT_TIME);

        long secondUserDue = reviewStateRepository.countDueStates(
                secondUserId,
                scheduledStages,
                CURRENT_TIME);

        List<ReviewActivityEvent> secondUserActivity = reviewLogRepository.findActivityEvents(
                        secondUserId,
                        CURRENT_TIME.minusSeconds(86400),
                        CURRENT_TIME);

        // then
        assertThat(firstUserDue).isEqualTo(1);
        assertThat(secondUserDue).isEqualTo(1);

        assertThat(secondUserActivity).containsExactly(
                new ReviewActivityEvent(
                        CURRENT_TIME.minusSeconds(90),
                        ReviewRating.EASY,
                        ReviewStage.REVIEW));
    }

    private User persistUser(String nickname) {
        return entityManager.persist(
                TestEntities.userBuilder()
                        .userId(null)
                        .nickname(nickname)
                        .email(nickname + "@example.com")
                        .build());
    }

    private FlashcardSet persistSet(
            User user,
            String name
    ) {
        return entityManager.persist(
                TestEntities.flashcardSetBuilder()
                        .setId(null)
                        .user(user)
                        .setName(name)
                        .build());
    }

    private Flashcard persistFlashcard(
            FlashcardSet flashcardSet,
            String term
    ) {
        return entityManager.persist(
                TestEntities.flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(flashcardSet)
                        .term(term)
                        .definition("Test definition")
                        .build());
    }

    private FlashcardReviewState persistState(
            User user,
            Flashcard flashcard,
            ReviewStage stage,
            Instant dueAt
    ) {
        return entityManager.persist(
                FlashcardReviewState.builder()
                        .user(user)
                        .flashcard(flashcard)
                        .state(stage)
                        .dueAt(dueAt)
                        .build());
    }

    private void persistLog(
            FlashcardReviewState reviewState,
            ReviewRating rating,
            ReviewStage stageBefore,
            Instant reviewedAt) {
        entityManager.persist(
                FlashcardReviewLog.builder()
                        .reviewState(reviewState)
                        .rating(rating)
                        .reviewedAt(reviewedAt)
                        .nextDueAt(reviewState.getDueAt())
                        .stageBefore(stageBefore)
                        .stageAfter(reviewState.getState())
                        .cardDataAfter("{}")
                        .algorithmVersion("FSRS-6")
                        .requestId(UUID.randomUUID())
                        .build());
    }
}
