
package com.brainbooster.spacedrepetition;

import com.brainbooster.exception.ReviewConflictException;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.integration.AbstractRepositoryTest;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.spacedrepetition.dto.ReviewResultDTO;
import com.brainbooster.spacedrepetition.dto.SubmitReviewRequest;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLogRepository;
import com.brainbooster.spacedrepetition.history.ReviewRating;
import com.brainbooster.spacedrepetition.scheduler.JavaFsrsSchedulerAdapter;
import com.brainbooster.spacedrepetition.service.ReviewSchedulingService;
import com.brainbooster.spacedrepetition.state.FlashcardReviewState;
import com.brainbooster.spacedrepetition.state.FlashcardReviewStateRepository;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies concurrent FSRS review submissions using PostgreSQL.
 *
 * <p>Each worker invokes the real transactional application service
 * on a separate thread and database transaction.
 *
 * <p>The surrounding test transaction is disabled so test fixtures
 * can be committed before concurrent requests begin.
 */
@Import({
        ReviewSchedulingService.class,
        JavaFsrsSchedulerAdapter.class,
        ConcurrentReviewIntegrationTest.TestConfig.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ConcurrentReviewIntegrationTest extends AbstractRepositoryTest {

    private static final Instant REVIEWED_AT = Instant.parse("2026-11-09T12:00:00Z");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReviewSchedulingService reviewSchedulingService;

    @Autowired
    private FlashcardReviewStateRepository reviewStateRepository;

    @Autowired
    private FlashcardReviewLogRepository reviewLogRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    private Long learningUserId;
    private Long flashcardSetId;
    private Long flashcardId;

    @BeforeEach
    void setUp() {
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        // commit fixtures before starting concurrent transactions
        transactionTemplate.executeWithoutResult(status -> {
            User learningUser = entityManager.persist(
                    TestEntities.userBuilder()
                            .userId(null)
                            .nickname("concurrent_review_user")
                            .email("concurrent_review_user@example.com")
                            .build());

            FlashcardSet flashcardSet = entityManager.persist(
                    TestEntities.flashcardSetBuilder()
                            .setId(null)
                            .user(learningUser)
                            .setName("Concurrent review test")
                            .build());

            Flashcard flashcard = entityManager.persist(
                    TestEntities.flashcardBuilder()
                            .flashcardId(null)
                            .flashcardSet(flashcardSet)
                            .term("Dependency Injection")
                            .definition("Supplying dependencies externally")
                            .build());

            entityManager.flush();

            learningUserId = learningUser.getUserId();
            flashcardSetId = flashcardSet.getSetId();
            flashcardId = flashcard.getFlashcardId();
        });

        when(currentUserProvider.getCurrentUser())
                .thenReturn(new AuthenticatedUser(learningUserId, Role.USER));
    }

    @AfterEach
    void cleanUp() {
        if (learningUserId == null) {
            return;
        }

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // concurrent tests commit real data and require explicit cleanup
        transactionTemplate.executeWithoutResult(status -> {
            jdbcTemplate.update("""
                    DELETE FROM flashcard_review_logs
                    WHERE review_state_id IN (
                        SELECT review_state_id
                        FROM flashcard_review_states
                        WHERE user_id = ?
                    )
                    """, learningUserId);

            jdbcTemplate.update(
                    "DELETE FROM flashcard_review_states WHERE user_id = ?",
                    learningUserId);

            jdbcTemplate.update(
                    "DELETE FROM flashcard WHERE flashcard_id = ?",
                    flashcardId);

            jdbcTemplate.update(
                    "DELETE FROM flashcard_set WHERE set_id = ?",
                    flashcardSetId);

            jdbcTemplate.update(
                    "DELETE FROM \"user\" WHERE user_id = ?",
                    learningUserId);
        });
    }

    @Test
    void submitReview_shouldPersistOnce_whenIdenticalRequestsAreConcurrent()
            throws InterruptedException, ExecutionException, TimeoutException {
        // given
        UUID requestId = UUID.randomUUID();

        SubmitReviewRequest reviewRequest = new SubmitReviewRequest(
                flashcardId,
                ReviewRating.GOOD,
                requestId);

        // when
        List<ReviewSubmissionOutcome> outcomes = executeConcurrently(reviewRequest, reviewRequest);

        // then
        assertThat(outcomes)
                .allSatisfy(outcome -> {
                    assertThat(outcome.conflict()).isNull();
                    assertThat(outcome.result()).isNotNull();});

        assertThat(outcomes.get(0).result()).isEqualTo(outcomes.get(1).result());

        assertThat(reviewLogRepository.count()).isEqualTo(1L);

        FlashcardReviewState persistedState = reviewStateRepository
                .findByUser_UserIdAndFlashcard_FlashcardId(
                        learningUserId,
                        flashcardId).orElseThrow();

        assertThat(persistedState.getReviewCount()).isEqualTo(1);
    }

    @Test
    void submitReview_shouldRejectSecondRequest_whenDifferentRequestsAreConcurrent()
            throws InterruptedException, ExecutionException, TimeoutException {
        // given
        SubmitReviewRequest firstRequest = new SubmitReviewRequest(
                flashcardId,
                ReviewRating.GOOD,
                UUID.randomUUID());

        SubmitReviewRequest secondRequest = new SubmitReviewRequest(
                flashcardId,
                ReviewRating.GOOD,
                UUID.randomUUID());

        // when
        List<ReviewSubmissionOutcome> outcomes = executeConcurrently(
                firstRequest,
                secondRequest);

        // then
        assertThat(outcomes)
                .filteredOn(outcome -> outcome.result() != null)
                .hasSize(1);

        assertThat(outcomes)
                .filteredOn(outcome -> outcome.conflict() != null)
                .hasSize(1)
                .first()
                .satisfies(outcome ->
                        assertThat(outcome.conflict())
                                .hasMessage("Flashcard is not due for review yet."));

        assertThat(reviewLogRepository.count()).isEqualTo(1L);

        FlashcardReviewState persistedState = reviewStateRepository
                .findByUser_UserIdAndFlashcard_FlashcardId(
                        learningUserId,
                        flashcardId).orElseThrow();

        assertThat(persistedState.getReviewCount()).isEqualTo(1);
    }

    /**
     * Starts two independent review submissions at approximately
     * the same time and collects their results.
     */
    private List<ReviewSubmissionOutcome> executeConcurrently(
            SubmitReviewRequest firstRequest,
            SubmitReviewRequest secondRequest
    ) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch workersReady = new CountDownLatch(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        try {
            Future<ReviewSubmissionOutcome> firstFuture = executor.submit(
                    () -> executeAfterSignal(
                            firstRequest,
                            workersReady,
                            startSignal));

            Future<ReviewSubmissionOutcome> secondFuture = executor.submit(
                    () -> executeAfterSignal(
                            secondRequest,
                            workersReady,
                            startSignal));

            // synchronize worker start without relying on Thread.sleep()
            assertThat(workersReady.await(10, TimeUnit.SECONDS)).isTrue();

            startSignal.countDown();

            ReviewSubmissionOutcome firstOutcome = firstFuture.get(30, TimeUnit.SECONDS);

            ReviewSubmissionOutcome secondOutcome = secondFuture.get(30, TimeUnit.SECONDS);

            return List.of(firstOutcome, secondOutcome);
        } finally {
            startSignal.countDown();
            executor.shutdownNow();
        }
    }

    /**
     * Submits a review after the shared start signal is released.
     *
     * <p>Expected business conflicts are captured as test results.
     * Unexpected exceptions propagate and fail the test.
     */
    private ReviewSubmissionOutcome executeAfterSignal(
            SubmitReviewRequest reviewRequest,
            CountDownLatch workersReady,
            CountDownLatch startSignal
    ) throws InterruptedException {
        workersReady.countDown();

        if (!startSignal.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concurrent review start signal timed out.");
        }

        try {
            ReviewResultDTO result = reviewSchedulingService.submitReview(reviewRequest);

            return new ReviewSubmissionOutcome(result, null);
        } catch (ReviewConflictException exception) {
            return new ReviewSubmissionOutcome(null, exception);
        }
    }

    /**
     * Holds the accepted review or its expected business conflict.
     *
     * @param result accepted review, if successful
     * @param conflict expected conflict, if rejected
     */
    private record ReviewSubmissionOutcome(
            ReviewResultDTO result,
            ReviewConflictException conflict
    ) {
    }

    /**
     * Provides authentication and a deterministic clock for the test
     */
    @TestConfiguration
    static class TestConfig {

        @Bean
        CurrentUserProvider currentUserProvider() {
            return mock(CurrentUserProvider.class);
        }

        @Bean
        Clock reviewClock() {
            return Clock.fixed(REVIEWED_AT, ZoneOffset.UTC);
        }
    }
}
