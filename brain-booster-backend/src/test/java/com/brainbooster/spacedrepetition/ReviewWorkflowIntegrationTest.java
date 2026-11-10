
package com.brainbooster.spacedrepetition;

import com.brainbooster.exception.ReviewConflictException;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.integration.AbstractRepositoryTest;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.spacedrepetition.dto.ReviewCardDTO;
import com.brainbooster.spacedrepetition.dto.ReviewResultDTO;
import com.brainbooster.spacedrepetition.dto.SubmitReviewRequest;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLog;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLogRepository;
import com.brainbooster.spacedrepetition.history.ReviewRating;
import com.brainbooster.spacedrepetition.scheduler.JavaFsrsSchedulerAdapter;
import com.brainbooster.spacedrepetition.service.ReviewDailyLimitService;
import com.brainbooster.spacedrepetition.service.ReviewSchedulingService;
import com.brainbooster.spacedrepetition.service.ReviewSessionService;
import com.brainbooster.spacedrepetition.settings.UserReviewSettings;
import com.brainbooster.spacedrepetition.settings.UserReviewSettingsRepository;
import com.brainbooster.spacedrepetition.state.FlashcardReviewState;
import com.brainbooster.spacedrepetition.state.FlashcardReviewStateRepository;
import com.brainbooster.spacedrepetition.state.ReviewStage;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.utils.TestEntities;
import io.github.openspacedrepetition.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the complete spaced repetition workflow using PostgreSQL.
 *
 * <p>The tests exercise the real FSRS adapter and JPA repositories
 * while replacing only the authenticated user provider.
 *
 * <p>Transactions are rolled back after every test by the
 * repository test infrastructure.
 */
@Import({
        ReviewSchedulingService.class,
        ReviewSessionService.class,
        ReviewDailyLimitService.class,
        JavaFsrsSchedulerAdapter.class,
        ReviewWorkflowIntegrationTest.TestConfig.class
})
class ReviewWorkflowIntegrationTest extends AbstractRepositoryTest {

    private static final UUID FIRST_REQUEST_ID = UUID.fromString(
            "e51773b7-a113-446b-9efd-b257e3301990");

    private static final UUID SECOND_REQUEST_ID = UUID.fromString(
            "cd574c77-0d8e-4c49-8828-54a259832ae5");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewSchedulingService reviewSchedulingService;

    @Autowired
    private ReviewSessionService reviewSessionService;

    @Autowired
    private FlashcardReviewStateRepository reviewStateRepository;

    @Autowired
    private FlashcardReviewLogRepository reviewLogRepository;

    @Autowired
    private UserReviewSettingsRepository reviewSettingsRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private Clock clock;


    private User learningUser;
    private FlashcardSet flashcardSet;
    private Flashcard firstFlashcard;
    private Flashcard secondFlashcard;

    @BeforeEach
    void setUp() {
        learningUser = persistUser("review_learning_user");

        flashcardSet = entityManager.persist(
                TestEntities.flashcardSetBuilder()
                        .setId(null)
                        .user(learningUser)
                        .setName("Java fundamentals")
                        .build());

        firstFlashcard = persistFlashcard(
                "Dependency Injection",
                "Supplying dependencies externally");

        secondFlashcard = persistFlashcard(
                "Polymorphism",
                "Multiple implementations of one interface");

        entityManager.flush();

        authenticateUser(learningUser);
    }


    @Test
    void submitReview_shouldPersistConsistentStateAndHistory_whenCardIsNew() {
        // given
        SubmitReviewRequest reviewRequest = new SubmitReviewRequest(
                firstFlashcard.getFlashcardId(),
                ReviewRating.GOOD,
                FIRST_REQUEST_ID);

        // when
        ReviewResultDTO actualResult = reviewSchedulingService.submitReview(reviewRequest);

        entityManager.flush();
        entityManager.clear();

        // then
        FlashcardReviewState persistedState = reviewStateRepository
                .findByUser_UserIdAndFlashcard_FlashcardId(
                        learningUser.getUserId(),
                        firstFlashcard.getFlashcardId())
                .orElseThrow();

        FlashcardReviewLog persistedLog = reviewLogRepository
                .findByRequestId(FIRST_REQUEST_ID)
                .orElseThrow();

        Card restoredCard = Card.fromJson(persistedState.getFsrsCardData());

        assertThat(persistedState.getReviewCount()).isEqualTo(1);

        assertThat(persistedState.getFsrsCardData()).isNotBlank();

        assertThat(persistedLog.getStageBefore()).isEqualTo(ReviewStage.NEW);

        assertThat(persistedLog.getRating()).isEqualTo(ReviewRating.GOOD);

        assertThat(persistedLog.getCardDataAfter()).isEqualTo(persistedState.getFsrsCardData());

        // compare the same instant using PostgreSQL timestamp precision
        assertThat(restoredCard.getDue().truncatedTo(ChronoUnit.MICROS))
                .isEqualTo(persistedState.getDueAt());

        assertThat(persistedLog.getNextDueAt()).isEqualTo(persistedState.getDueAt());

        assertThat(actualResult.nextReviewAt()).isEqualTo(persistedState.getDueAt());

        assertThat(persistedLog.getReviewedAt()).isEqualTo(persistedState.getLastReviewedAt());

        assertThat(actualResult.reviewedAt()).isEqualTo(persistedLog.getReviewedAt());

        assertThat(actualResult.nextReviewAt()).isAfter(actualResult.reviewedAt());
    }



    @Test
    void submitReview_shouldReturnExistingResult_whenRequestIsRetried() {
        // given
        SubmitReviewRequest reviewRequest = new SubmitReviewRequest(
                firstFlashcard.getFlashcardId(),
                ReviewRating.GOOD,
                FIRST_REQUEST_ID);

        ReviewResultDTO firstResult = reviewSchedulingService.submitReview(reviewRequest);

        // Flush pending changes and clear the persistence context
        // to ensure the retry reads the original review from PostgreSQL.
        entityManager.flush();
        entityManager.clear();

        // when
        ReviewResultDTO repeatedResult = reviewSchedulingService.submitReview(reviewRequest);

        // then
        assertThat(repeatedResult).isEqualTo(firstResult);

        // retrying the request must not create another review log
        assertThat(reviewLogRepository.count()).isEqualTo(1L);

        FlashcardReviewState persistedState = reviewStateRepository
                .findByUser_UserIdAndFlashcard_FlashcardId(
                        learningUser.getUserId(),
                        firstFlashcard.getFlashcardId()).orElseThrow();

        // FSRS must not apply the same rating twice
        assertThat(persistedState.getReviewCount()).isEqualTo(1);
    }


    @Test
    void submitReview_shouldRejectRequest_whenRequestIdHasDifferentRating() {
        // given
        reviewSchedulingService.submitReview(new SubmitReviewRequest(
                firstFlashcard.getFlashcardId(),
                ReviewRating.GOOD,
                FIRST_REQUEST_ID));

        SubmitReviewRequest conflictingRequest = new SubmitReviewRequest(
                firstFlashcard.getFlashcardId(),
                ReviewRating.EASY,
                FIRST_REQUEST_ID);

        // when
        // then
        assertThatThrownBy(() -> reviewSchedulingService.submitReview(conflictingRequest))
                .isInstanceOf(ReviewConflictException.class)
                .hasMessageContaining("Request ID has already been used");

        assertThat(reviewLogRepository.count()).isEqualTo(1L);
    }

    @Test
    void submitReview_shouldRejectRating_whenCardIsNotDue() {
        // given
        reviewSchedulingService.submitReview(new SubmitReviewRequest(
                firstFlashcard.getFlashcardId(),
                ReviewRating.GOOD,
                FIRST_REQUEST_ID));

        SubmitReviewRequest earlyReviewRequest = new SubmitReviewRequest(
                firstFlashcard.getFlashcardId(),
                ReviewRating.GOOD,
                SECOND_REQUEST_ID);

        // when
        // then
        assertThatThrownBy(() -> reviewSchedulingService.submitReview(earlyReviewRequest))
                .isInstanceOf(ReviewConflictException.class)
                .hasMessage("Flashcard is not due for review yet.");

        assertThat(reviewLogRepository.count()).isEqualTo(1L);
    }

    @Test
    void submitReview_shouldRejectReview_whenFlashcardBelongsToAnotherUser() {
        // given
        User anotherUser = persistUser("another_review_user");

        entityManager.flush();
        authenticateUser(anotherUser);

        SubmitReviewRequest reviewRequest = new SubmitReviewRequest(
                firstFlashcard.getFlashcardId(),
                ReviewRating.GOOD,
                FIRST_REQUEST_ID);

        // when
        // then
        assertThatThrownBy(() -> reviewSchedulingService.submitReview(reviewRequest))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(reviewLogRepository.count()).isZero();
    }

    @Test
    void getReviewQueue_shouldReturnDueAndNewCards_whenBothExist() {
        // given
        Instant currentTime = clock.instant();

        FlashcardReviewState dueReviewState = FlashcardReviewState.builder()
                        .user(learningUser)
                        .flashcard(firstFlashcard)
                        .state(ReviewStage.REVIEW)
                        .dueAt(currentTime.minusSeconds(300))
                        .build();

        reviewStateRepository.saveAndFlush(dueReviewState);
        entityManager.clear();

        // when
        List<ReviewCardDTO> reviewQueue = reviewSessionService.getReviewQueue(
                        flashcardSet.getSetId());

        // then
        assertThat(reviewQueue)
                .extracting(ReviewCardDTO::flashcardId)
                .containsExactly(
                        firstFlashcard.getFlashcardId(),
                        secondFlashcard.getFlashcardId());

        assertThat(reviewQueue.get(0).stage()).isEqualTo(ReviewStage.REVIEW);

        assertThat(reviewQueue.get(1).stage()).isEqualTo(ReviewStage.NEW);

        assertThat(reviewQueue.get(1).dueAt()).isNull();
    }

    @Test
    void getReviewQueue_shouldRespectDailyLimits_whenNewCardLimitIsZero() {
        // given
        reviewSettingsRepository.saveAndFlush(
                UserReviewSettings.builder()
                        .user(learningUser)
                        .newCardsPerDay(0)
                        .reviewLimitPerDay(10)
                        .build());

        reviewStateRepository.saveAndFlush(
                FlashcardReviewState.builder()
                        .user(learningUser)
                        .flashcard(firstFlashcard)
                        .state(ReviewStage.REVIEW)
                        .dueAt(clock.instant().minusSeconds(60))
                        .build());

        entityManager.clear();

        // when
        List<ReviewCardDTO> reviewQueue = reviewSessionService.getReviewQueue(
                flashcardSet.getSetId());

        // then
        assertThat(reviewQueue)
                .extracting(ReviewCardDTO::flashcardId)
                .containsExactly(firstFlashcard.getFlashcardId());
    }

    @Test
    void getReviewQueue_shouldRejectAccess_whenSetBelongsToAnotherUser() {
        // given
        User anotherUser = persistUser("unauthorized_review_user");

        entityManager.flush();
        authenticateUser(anotherUser);

        // when
        Long setId = flashcardSet.getSetId();
        // then
        assertThatThrownBy(() -> reviewSessionService.getReviewQueue(setId))
                .isInstanceOf(AccessDeniedException.class);
    }


    @Test
    void submitReview_shouldRejectSecondNewCard_whenDailyLimitIsReached() {
        // given
        reviewSettingsRepository.saveAndFlush(
                UserReviewSettings.builder()
                        .user(learningUser)
                        .newCardsPerDay(1)
                        .reviewLimitPerDay(200)
                        .timezone("Europe/Warsaw")
                        .build());

        reviewSchedulingService.submitReview(
                new SubmitReviewRequest(
                        firstFlashcard.getFlashcardId(),
                        ReviewRating.GOOD,
                        FIRST_REQUEST_ID));

        entityManager.flush();

        // when
        SubmitReviewRequest secondRequest = new SubmitReviewRequest(
                        secondFlashcard.getFlashcardId(),
                        ReviewRating.GOOD,
                        SECOND_REQUEST_ID);

        // then
        assertThatThrownBy(() -> reviewSchedulingService.submitReview(secondRequest)
        )
                .isInstanceOf(ReviewConflictException.class)
                .hasMessage("Daily new card limit reached.");

        assertThat(reviewLogRepository.count()).isEqualTo(1);
    }

    @Test
    void submitReview_shouldReturnOriginalResult_whenLimitIsReachedAndRequestIsRetried() {
        // given
        reviewSettingsRepository.saveAndFlush(
                UserReviewSettings.builder()
                        .user(learningUser)
                        .newCardsPerDay(1)
                        .build());

        SubmitReviewRequest reviewRequest = new SubmitReviewRequest(
                        firstFlashcard.getFlashcardId(),
                        ReviewRating.GOOD,
                        FIRST_REQUEST_ID);

        ReviewResultDTO originalResult = reviewSchedulingService.submitReview(reviewRequest);

        entityManager.flush();
        entityManager.clear();

        // when
        ReviewResultDTO repeatedResult = reviewSchedulingService.submitReview(reviewRequest);

        // then
        assertThat(repeatedResult).isEqualTo(originalResult);
        assertThat(reviewLogRepository.count()).isEqualTo(1);
    }

    @Test
    void getReviewQueue_shouldExcludeNewCards_whenDailyLimitIsReached() {
        // given
        reviewSettingsRepository.saveAndFlush(
                UserReviewSettings.builder()
                        .user(learningUser)
                        .newCardsPerDay(1)
                        .build());

        reviewSchedulingService.submitReview(
                new SubmitReviewRequest(
                        firstFlashcard.getFlashcardId(),
                        ReviewRating.GOOD,
                        FIRST_REQUEST_ID));

        entityManager.flush();

        // when
        List<ReviewCardDTO> reviewQueue = reviewSessionService.getReviewQueue(
                        flashcardSet.getSetId());

        // then
        assertThat(reviewQueue).isEmpty();
    }


    @Test
    void submitReview_shouldRejectDueReview_whenDailyReviewLimitIsZero() {
        // given
        reviewSettingsRepository.saveAndFlush(
                UserReviewSettings.builder()
                        .user(learningUser)
                        .reviewLimitPerDay(0)
                        .build());

        reviewStateRepository.saveAndFlush(
                FlashcardReviewState.builder()
                        .user(learningUser)
                        .flashcard(firstFlashcard)
                        .state(ReviewStage.REVIEW)
                        .dueAt(clock.instant().minusSeconds(60))
                        .build());

        SubmitReviewRequest reviewRequest = new SubmitReviewRequest(
                        firstFlashcard.getFlashcardId(),
                        ReviewRating.GOOD,
                        FIRST_REQUEST_ID);

        // when
        // then
        assertThatThrownBy(() ->
                reviewSchedulingService.submitReview(reviewRequest)
        )
                .isInstanceOf(ReviewConflictException.class)
                .hasMessage("Daily review limit reached.");

        assertThat(reviewLogRepository.count()).isZero();
    }

    @Test
    void getReviewQueue_shouldIncludeLearningCards_whenBothDailyLimitsAreZero() {
        // given
        reviewSettingsRepository.saveAndFlush(
                UserReviewSettings.builder()
                        .user(learningUser)
                        .newCardsPerDay(0)
                        .reviewLimitPerDay(0)
                        .build());

        reviewStateRepository.saveAndFlush(
                FlashcardReviewState.builder()
                        .user(learningUser)
                        .flashcard(firstFlashcard)
                        .state(ReviewStage.LEARNING)
                        .dueAt(clock.instant().minusSeconds(60))
                        .build());

        entityManager.flush();
        entityManager.clear();

        // when
        List<ReviewCardDTO> reviewQueue = reviewSessionService.getReviewQueue(flashcardSet.getSetId());

        // then
        assertThat(reviewQueue)
                .extracting(ReviewCardDTO::flashcardId)
                .containsExactly(firstFlashcard.getFlashcardId());

        assertThat(reviewQueue.get(0).stage())
                .isEqualTo(ReviewStage.LEARNING);
    }



    private User persistUser(String nickname) {
        return entityManager.persist(
                TestEntities.userBuilder()
                        .userId(null)
                        .nickname(nickname)
                        .email(nickname + "@example.com")
                        .build());
    }

    private Flashcard persistFlashcard(
            String term,
            String definition
    ) {
        return entityManager.persist(
                TestEntities.flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(flashcardSet)
                        .term(term)
                        .definition(definition)
                        .build());
    }

    private void authenticateUser(User user) {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new AuthenticatedUser(
                        user.getUserId(),
                        Role.USER));
    }

    /**
     * Provides only the dependencies missing from the JPA test slice
     */
    @TestConfiguration
    static class TestConfig {

        @Bean
        CurrentUserProvider currentUserProvider() {
            return mock(CurrentUserProvider.class);
        }

        @Bean
        Clock reviewClock() {
            return Clock.systemUTC();
        }
    }
}
