package com.brainbooster.spacedrepetition;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.integration.AbstractRepositoryTest;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLog;
import com.brainbooster.spacedrepetition.history.FlashcardReviewLogRepository;
import com.brainbooster.spacedrepetition.history.ReviewRating;
import com.brainbooster.spacedrepetition.settings.UserReviewSettings;
import com.brainbooster.spacedrepetition.settings.UserReviewSettingsRepository;
import com.brainbooster.spacedrepetition.state.FlashcardReviewState;
import com.brainbooster.spacedrepetition.state.FlashcardReviewStateRepository;
import com.brainbooster.spacedrepetition.state.ReviewStage;
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
import static org.assertj.core.api.Assertions.catchThrowable;

class SpacedRepetitionRepositoryTest extends AbstractRepositoryTest {

    private static final Instant REVIEWED_AT = Instant.parse("2026-10-15T10:00:00Z");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FlashcardReviewStateRepository reviewStateRepository;

    @Autowired
    private FlashcardReviewLogRepository reviewLogRepository;

    @Autowired
    private UserReviewSettingsRepository reviewSettingsRepository;

    private User firstUser;
    private User secondUser;
    private FlashcardSet firstSet;
    private Flashcard firstFlashcard;
    private Flashcard secondFlashcard;

    @BeforeEach
    void setUp() {
        firstUser = persistUser("first_review_user");
        secondUser = persistUser("second_review_user");
        firstSet = persistFlashcardSet(firstUser, "Java");
        firstFlashcard = persistFlashcard(firstSet, "class", "A type definition");
        secondFlashcard = persistFlashcard(firstSet, "interface", "A contract");
        entityManager.flush();
    }

    @Test
    void findByUserIdAndFlashcardId_shouldReturnOwnState_whenTwoUsersStudySameFlashcard() {
        // given
        persistReviewState(firstUser, firstFlashcard, REVIEWED_AT);
        persistReviewState(secondUser, firstFlashcard, REVIEWED_AT.plusSeconds(3600));
        entityManager.flush();
        entityManager.clear();

        // when
        FlashcardReviewState actualState = reviewStateRepository
                .findByUser_UserIdAndFlashcard_FlashcardId(
                        firstUser.getUserId(),
                        firstFlashcard.getFlashcardId())
                .orElseThrow();

        // then
        assertThat(actualState.getUser().getUserId())
                .isEqualTo(firstUser.getUserId());
        assertThat(actualState.getDueAt()).isEqualTo(REVIEWED_AT);
    }

    @Test
    void findDueStates_shouldFilterByUserAndSet_whenSomeCardsAreNotDue() {
        // given
        Instant now = REVIEWED_AT;
        FlashcardSet secondSet = persistFlashcardSet(firstUser, "Databases");
        Flashcard flashcardInSecondSet = persistFlashcard(secondSet, "index", "Query acceleration");

        persistReviewState(firstUser, firstFlashcard, now.minusSeconds(60));
        persistReviewState(firstUser, secondFlashcard, now.plusSeconds(60));
        persistReviewState(firstUser, flashcardInSecondSet, now.minusSeconds(120));
        persistReviewState(secondUser, firstFlashcard, now.minusSeconds(180));
        entityManager.flush();
        entityManager.clear();

        // when
        List<FlashcardReviewState> actualDueStates = reviewStateRepository
                .findByUser_UserIdAndFlashcard_FlashcardSet_SetIdAndDueAtLessThanEqualOrderByDueAtAsc(
                        firstUser.getUserId(),
                        firstSet.getSetId(),
                        now);

        // then
        assertThat(actualDueStates)
                .extracting(state -> state.getFlashcard().getFlashcardId())
                .containsExactly(firstFlashcard.getFlashcardId());
    }

    @Test
    void saveAndFlush_shouldRejectDuplicateState_whenUserAndFlashcardAlreadyExist() {
        // given
        persistReviewState(firstUser, firstFlashcard, REVIEWED_AT);
        entityManager.flush();

        FlashcardReviewState duplicateState = FlashcardReviewState.builder()
                .user(firstUser)
                .flashcard(firstFlashcard)
                .dueAt(REVIEWED_AT.plusSeconds(60))
                .build();

        // when
        Throwable actualFailure = catchThrowable(
                () -> reviewStateRepository.saveAndFlush(duplicateState));

        // then
        assertThat(actualFailure)
                .isNotNull()
                .hasStackTraceContaining("uk_flashcard_review_states_user_flashcard");
    }

    @Test
    void findByRequestId_shouldReturnReviewLog_whenRequestWasPreviouslySaved() {
        // given
        FlashcardReviewState reviewState = persistReviewState(firstUser, firstFlashcard, REVIEWED_AT);
        UUID requestId = UUID.fromString("0ac014fe-c5fe-47cb-81a7-9a86a583703a");

        persistReviewLog(
                reviewState,
                requestId,
                REVIEWED_AT,
                ReviewRating.GOOD
        );
        entityManager.flush();
        entityManager.clear();

        // when
        FlashcardReviewLog actualReviewLog = reviewLogRepository
                .findByRequestId(requestId)
                .orElseThrow();

        // then
        assertThat(actualReviewLog.getRating()).isEqualTo(ReviewRating.GOOD);
        assertThat(actualReviewLog.getReviewState().getUser().getUserId())
                .isEqualTo(firstUser.getUserId());
    }

    @Test
    void saveAndFlush_shouldRejectDuplicateReviewLog_whenRequestIdAlreadyExists() {
        // given
        FlashcardReviewState reviewState = persistReviewState(firstUser, firstFlashcard, REVIEWED_AT);
        UUID requestId = UUID.fromString("d9e5814c-3049-4e98-b076-55b54edcc7fe");

        persistReviewLog(reviewState, requestId, REVIEWED_AT, ReviewRating.GOOD);
        entityManager.flush();

        FlashcardReviewLog duplicateReviewLog = FlashcardReviewLog.builder()
                .reviewState(reviewState)
                .requestId(requestId)
                .rating(ReviewRating.EASY)
                .reviewedAt(REVIEWED_AT.plusSeconds(60))
                .nextDueAt(REVIEWED_AT.plusSeconds(86400))
                .stageBefore(ReviewStage.REVIEW)
                .stageAfter(ReviewStage.REVIEW)
                .cardDataAfter("{\"state\":\"REVIEW\"}")
                .algorithmVersion("FSRS-6")
                .build();

        // when
        Throwable actualFailure = catchThrowable(() -> reviewLogRepository.saveAndFlush(duplicateReviewLog));

        // then
        assertThat(actualFailure)
                .isNotNull()
                .hasStackTraceContaining("uk_flashcard_review_logs_request_id");
    }

    @Test
    void findReviewHistory_shouldReturnOnlyUsersReviews_whenOtherUsersHaveHistory() {
        // given
        FlashcardReviewState firstState = persistReviewState(firstUser, firstFlashcard, REVIEWED_AT);
        FlashcardReviewState secondState = persistReviewState(firstUser, secondFlashcard, REVIEWED_AT);
        FlashcardReviewState otherUsersState = persistReviewState(secondUser, firstFlashcard, REVIEWED_AT);

        UUID firstRequestId = UUID.randomUUID();
        UUID secondRequestId = UUID.randomUUID();
        UUID otherUsersRequestId = UUID.randomUUID();

        persistReviewLog(firstState, firstRequestId,
                REVIEWED_AT.minusSeconds(120), ReviewRating.GOOD);
        persistReviewLog(secondState, secondRequestId,
                REVIEWED_AT.minusSeconds(60), ReviewRating.HARD);
        persistReviewLog(otherUsersState, otherUsersRequestId,
                REVIEWED_AT, ReviewRating.EASY);
        entityManager.flush();
        entityManager.clear();

        // when
        List<FlashcardReviewLog> actualHistory = reviewLogRepository
                .findByReviewState_User_UserIdOrderByReviewedAtDesc(
                        firstUser.getUserId());

        // then
        assertThat(actualHistory)
                .extracting(FlashcardReviewLog::getRequestId)
                .containsExactly(secondRequestId, firstRequestId);
    }

    @Test
    void findById_shouldReturnDefaultSettings_whenUserSettingsAreCreated() {
        // given
        UserReviewSettings settings = UserReviewSettings.builder()
                .user(firstUser)
                .build();
        reviewSettingsRepository.saveAndFlush(settings);
        entityManager.clear();

        // when
        UserReviewSettings actualSettings = reviewSettingsRepository
                .findById(firstUser.getUserId())
                .orElseThrow();

        // then
        assertThat(actualSettings.getDesiredRetention()).isEqualTo(0.90);
        assertThat(actualSettings.getNewCardsPerDay()).isEqualTo(20);
        assertThat(actualSettings.getReviewLimitPerDay()).isEqualTo(200);
        assertThat(actualSettings.getTimezone()).isEqualTo("UTC");
        assertThat(actualSettings.getAlgorithmVersion()).isEqualTo("FSRS-6");
    }

    private User persistUser(String nickname) {
        return entityManager.persist(
                TestEntities.userBuilder()
                        .userId(null)
                        .nickname(nickname)
                        .email(nickname + "@example.com")
                        .build());
    }

    private FlashcardSet persistFlashcardSet(User owner, String setName) {
        return entityManager.persist(
                TestEntities.flashcardSetBuilder()
                        .setId(null)
                        .user(owner)
                        .setName(setName)
                        .build());
    }

    private Flashcard persistFlashcard(
            FlashcardSet flashcardSet,
            String term,
            String definition) {
        return entityManager.persist(
                new Flashcard(null, flashcardSet, term, definition));
    }

    private FlashcardReviewState persistReviewState(
            User user,
            Flashcard flashcard,
            Instant dueAt
    ) {
        return reviewStateRepository.save(
                FlashcardReviewState.builder()
                        .user(user)
                        .flashcard(flashcard)
                        .state(ReviewStage.REVIEW)
                        .dueAt(dueAt)
                        .stability(5.0)
                        .difficulty(4.0)
                        .reviewCount(1)
                        .fsrsCardData("{\"state\":\"REVIEW\"}")
                        .build());
    }

    private FlashcardReviewLog persistReviewLog(
            FlashcardReviewState reviewState,
            UUID requestId,
            Instant reviewedAt,
            ReviewRating rating
    ) {
        return reviewLogRepository.save(
                FlashcardReviewLog.builder()
                        .reviewState(reviewState)
                        .requestId(requestId)
                        .rating(rating)
                        .reviewedAt(reviewedAt)
                        .previousDueAt(reviewedAt.minusSeconds(60))
                        .nextDueAt(reviewedAt.plusSeconds(86400))
                        .stageBefore(ReviewStage.LEARNING)
                        .stageAfter(ReviewStage.REVIEW)
                        .cardDataBefore("{\"state\":\"LEARNING\"}")
                        .cardDataAfter("{\"state\":\"REVIEW\"}")
                        .algorithmVersion("FSRS-6")
                        .build());
    }
}
