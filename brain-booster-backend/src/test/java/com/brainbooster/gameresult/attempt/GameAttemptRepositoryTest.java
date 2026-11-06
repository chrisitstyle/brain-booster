
package com.brainbooster.gameresult.attempt;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.gameresult.GameMode;
import com.brainbooster.integration.AbstractRepositoryTest;
import com.brainbooster.user.User;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GameAttemptRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private GameAttemptRepository gameAttemptRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findSummaryByUserIdAndSetId_shouldAggregateStatistics_whenAttemptsExist() {
        User learningUser = persistUser("analytics_user");

        FlashcardSet vocabularySet = persistFlashcardSet(learningUser, "Vocabulary");

        Instant firstCompletedAt = Instant.parse("2026-08-01T10:00:00Z");

        Instant secondCompletedAt = Instant.parse("2026-08-02T10:00:00Z");

        Instant lastCompletedAt = Instant.parse("2026-08-03T10:00:00Z");

        persistCompletedGameAttempt(
                learningUser,
                vocabularySet,
                1,
                2,
                30,
                firstCompletedAt);

        persistCompletedGameAttempt(
                learningUser,
                vocabularySet,
                9,
                10,
                90,
                secondCompletedAt);

        persistCompletedGameAttempt(
                learningUser,
                vocabularySet,
                1,
                3,
                null,
                lastCompletedAt);

        entityManager.flush();
        entityManager.clear();

        GameAttemptSummaryProjection attemptSummary = gameAttemptRepository.findSummaryByUserIdAndSetId(
                        learningUser.getUserId(),
                        vocabularySet.getSetId());

        assertThat(attemptSummary.getTotalAttempts()).isEqualTo(3L);

        assertThat(attemptSummary.getTotalScore()).isEqualTo(11L);

        assertThat(attemptSummary.getTotalQuestions()).isEqualTo(15L);

        assertThat(attemptSummary.getAverageScore()).isCloseTo(11.0 / 3.0, within(0.00001));

        assertThat(attemptSummary.getBestScore()).isEqualTo(9);

        // AVG must ignore the attempt with NULL duration
        assertThat(attemptSummary.getAverageDurationSeconds()).isEqualTo(60.0);

        assertThat(attemptSummary.getLastCompletedAt()).isEqualTo(lastCompletedAt);
    }

    @Test
    void findSummaryByUserIdAndSetId_shouldReturnEmptyAggregates_whenNoAttemptsExist() {
        User learningUser = persistUser("analytics_empty_user");

        FlashcardSet vocabularySet = persistFlashcardSet(learningUser, "Vocabulary");

        entityManager.flush();
        entityManager.clear();

        GameAttemptSummaryProjection attemptSummary = gameAttemptRepository.findSummaryByUserIdAndSetId(
                        learningUser.getUserId(),
                        vocabularySet.getSetId());

        assertThat(attemptSummary.getTotalAttempts()).isZero();

        assertThat(attemptSummary.getTotalScore()).isNull();

        assertThat(attemptSummary.getTotalQuestions()).isNull();

        assertThat(attemptSummary.getAverageScore()).isNull();

        assertThat(attemptSummary.getBestScore()).isNull();

        assertThat(attemptSummary.getAverageDurationSeconds()).isNull();

        assertThat(attemptSummary.getLastCompletedAt()).isNull();
    }

    @Test
    void findSummaryByUserIdAndSetId_shouldExcludeUnrelatedAttempts_whenOtherUsersOrSetsExist() {
        User learningUser = persistUser("analytics_owner");
        User anotherUser = persistUser("analytics_other");

        FlashcardSet vocabularySet = persistFlashcardSet(learningUser, "Vocabulary");

        FlashcardSet grammarSet = persistFlashcardSet(learningUser, "Grammar");

        FlashcardSet anotherUsersSet = persistFlashcardSet(anotherUser, "Other vocabulary");

        Instant completedAt = Instant.parse("2026-08-01T10:00:00Z");

        // only this attempt should be included
        persistCompletedGameAttempt(
                learningUser,
                vocabularySet,
                1,
                2,
                20,
                completedAt);

        // Same user, different flashcard set
        persistCompletedGameAttempt(
                learningUser,
                grammarSet,
                9,
                10,
                100,
                completedAt.plusSeconds(60));

        // different user and different flashcard set
        persistCompletedGameAttempt(
                anotherUser,
                anotherUsersSet,
                8,
                10,
                120,
                completedAt.plusSeconds(120));

        entityManager.flush();
        entityManager.clear();

        GameAttemptSummaryProjection attemptSummary = gameAttemptRepository.findSummaryByUserIdAndSetId(
                        learningUser.getUserId(),
                        vocabularySet.getSetId());

        assertThat(attemptSummary.getTotalAttempts()).isEqualTo(1L);

        assertThat(attemptSummary.getTotalScore()).isEqualTo(1L);

        assertThat(attemptSummary.getTotalQuestions()).isEqualTo(2L);

        assertThat(attemptSummary.getAverageScore()).isEqualTo(1.0);

        assertThat(attemptSummary.getBestScore()).isEqualTo(1);

        assertThat(attemptSummary.getAverageDurationSeconds()).isEqualTo(20.0);

        assertThat(attemptSummary.getLastCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    void findSummaryByUserIdAndSetId_shouldReturnNullAverageDuration_whenAllDurationsAreNull() {
        User learningUser = persistUser("analytics_null_duration_user");

        FlashcardSet vocabularySet = persistFlashcardSet(learningUser, "Vocabulary");

        Instant firstCompletedAt = Instant.parse("2026-08-01T10:00:00Z");

        Instant secondCompletedAt = Instant.parse("2026-08-02T10:00:00Z");

        persistCompletedGameAttempt(
                learningUser,
                vocabularySet,
                2,
                4,
                null,
                firstCompletedAt);

        persistCompletedGameAttempt(
                learningUser,
                vocabularySet,
                1,
                2,
                null,
                secondCompletedAt);

        entityManager.flush();
        entityManager.clear();

        GameAttemptSummaryProjection attemptSummary = gameAttemptRepository.findSummaryByUserIdAndSetId(
                        learningUser.getUserId(),
                        vocabularySet.getSetId());

        assertThat(attemptSummary.getTotalAttempts()).isEqualTo(2L);

        assertThat(attemptSummary.getTotalScore()).isEqualTo(3L);

        assertThat(attemptSummary.getAverageDurationSeconds()).isNull();

        assertThat(attemptSummary.getLastCompletedAt()).isEqualTo(secondCompletedAt);
    }

    private User persistUser(String nickname) {
        User userToPersist = TestEntities.userBuilder()
                .userId(null)
                .nickname(nickname)
                .email(nickname + "@example.com")
                .build();

        return entityManager.persist(userToPersist);
    }

    private FlashcardSet persistFlashcardSet(User owner, String setName) {
        FlashcardSet flashcardSetToPersist = TestEntities.flashcardSetBuilder()
                        .setId(null)
                        .user(owner)
                        .setName(setName)
                        .build();

        return entityManager.persist(flashcardSetToPersist);
    }

    private void persistCompletedGameAttempt(
            User learningUser,
            FlashcardSet flashcardSet,
            int score,
            int totalQuestions,
            Integer durationSeconds,
            Instant completedAt
    ) {
        GameAttempt gameAttemptToPersist =
                TestEntities.gameAttemptBuilder()
                        .attemptId(null)
                        .user(learningUser)
                        .set(flashcardSet)
                        .mode(GameMode.MULTIPLE_CHOICE)
                        .score(score)
                        .totalQuestions(totalQuestions)
                        .durationSeconds(durationSeconds)
                        .completedAt(completedAt)
                        .build();

        entityManager.persist(gameAttemptToPersist);
    }
}
