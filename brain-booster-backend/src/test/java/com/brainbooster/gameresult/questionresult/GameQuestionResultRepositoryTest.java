
package com.brainbooster.gameresult.questionresult;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.gameresult.GameMode;
import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.attempt.GameAttempt;
import com.brainbooster.integration.AbstractRepositoryTest;
import com.brainbooster.user.User;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameQuestionResultRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private GameQuestionResultRepository gameQuestionResultRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findFlashcardStatisticsByUserIdAndSetId_shouldAggregateAnswers_whenAttemptsExist() {
        User learningUser = persistUser("learning_user");
        User anotherUser = persistUser("another_user");

        FlashcardSet vocabularySet = persistFlashcardSet(learningUser, "Vocabulary");

        Flashcard weakFlashcard = persistFlashcard(vocabularySet, "cat", "kot");

        Flashcard strongFlashcard = persistFlashcard(vocabularySet, "dog", "pies");

        GameAttempt learningAttempt = persistGameAttempt(learningUser, vocabularySet);

        GameAttempt anotherUsersAttempt = persistGameAttempt(anotherUser, vocabularySet);

        Instant firstAnsweredAt = Instant.parse("2026-08-01T10:00:00Z");

        Instant lastAnsweredAt = Instant.parse("2026-08-02T10:00:00Z");

        persistQuestionResult(
                learningAttempt,
                weakFlashcard,
                GameQuestionType.MULTIPLE_CHOICE,
                true,
                2,
                0,
                firstAnsweredAt);

        persistQuestionResult(
                learningAttempt,
                weakFlashcard,
                GameQuestionType.WRITTEN,
                false,
                1,
                1,
                lastAnsweredAt);

        persistQuestionResult(
                learningAttempt,
                strongFlashcard,
                GameQuestionType.MULTIPLE_CHOICE,
                true,
                0,
                2,
                lastAnsweredAt);

        persistQuestionResult(
                anotherUsersAttempt,
                weakFlashcard,
                GameQuestionType.WRITTEN,
                false,
                5,
                0,
                lastAnsweredAt);

        entityManager.flush();
        entityManager.clear();

        List<WeakFlashcardStatisticsProjection> flashcardStatistics = gameQuestionResultRepository
                .findFlashcardStatisticsByUserIdAndSetId(
                        learningUser.getUserId(),
                        vocabularySet.getSetId());

        assertThat(flashcardStatistics).hasSize(2);

        WeakFlashcardStatisticsProjection weakFlashcardStatistics =
                flashcardStatistics.stream()
                        .filter(statistics ->
                                statistics.getFlashcardId()
                                        .equals(weakFlashcard.getFlashcardId()))
                        .findFirst()
                        .orElseThrow();

        assertThat(weakFlashcardStatistics.getTerm()).isEqualTo("cat");

        assertThat(weakFlashcardStatistics.getDefinition()).isEqualTo("kot");

        assertThat(weakFlashcardStatistics.getTotalAnswers()).isEqualTo(2L);

        assertThat(weakFlashcardStatistics.getCorrectAnswers()).isEqualTo(1L);

        assertThat(weakFlashcardStatistics.getTotalMistakes()).isEqualTo(3L);

        assertThat(weakFlashcardStatistics.getLastAnsweredAt()).isEqualTo(lastAnsweredAt);
    }

    @Test
    void findQuestionTypeStatisticsByUserIdAndSetId_shouldAggregateAnswers_whenDifferentQuestionTypesExist() {
        User learningUser = persistUser("question_type_user");
        FlashcardSet vocabularySet = persistFlashcardSet(learningUser, "Vocabulary");

        Flashcard flashcard = persistFlashcard(vocabularySet, "cat", "kot");

        GameAttempt learningAttempt = persistGameAttempt(learningUser, vocabularySet);

        Instant answeredAt = Instant.parse("2026-08-01T10:00:00Z");

        persistQuestionResult(
                learningAttempt,
                flashcard,
                GameQuestionType.MULTIPLE_CHOICE,
                true,
                0,
                0,
                answeredAt);

        persistQuestionResult(
                learningAttempt,
                flashcard,
                GameQuestionType.MULTIPLE_CHOICE,
                false,
                0,
                1,
                answeredAt);

        persistQuestionResult(
                learningAttempt,
                flashcard,
                GameQuestionType.WRITTEN,
                true,
                0,
                2,
                answeredAt);

        entityManager.flush();
        entityManager.clear();

        List<QuestionTypeStatisticsProjection> questionTypeStatistics =
                gameQuestionResultRepository.findQuestionTypeStatisticsByUserIdAndSetId(
                        learningUser.getUserId(),
                        vocabularySet.getSetId());

        assertThat(questionTypeStatistics).hasSize(2);

        QuestionTypeStatisticsProjection multipleChoiceStatistics =
                questionTypeStatistics.stream()
                        .filter(statistics ->
                                statistics.getQuestionType()
                                        == GameQuestionType.MULTIPLE_CHOICE
                        ).findFirst()
                        .orElseThrow();

        assertThat(multipleChoiceStatistics.getTotalAnswers()).isEqualTo(2L);

        assertThat(multipleChoiceStatistics.getCorrectAnswers()).isEqualTo(1L);
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
                TestEntities.flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(flashcardSet)
                        .term(term)
                        .definition(definition)
                        .build());
    }

    private GameAttempt persistGameAttempt(
            User learningUser,
            FlashcardSet flashcardSet
    ) {return entityManager.persist(
                TestEntities.gameAttemptBuilder()
                        .attemptId(null)
                        .user(learningUser)
                        .set(flashcardSet)
                        .mode(GameMode.MULTIPLE_CHOICE)
                        .score(1)
                        .totalQuestions(3)
                        .build());
    }

    private void persistQuestionResult(
            GameAttempt gameAttempt,
            Flashcard flashcard,
            GameQuestionType questionType,
            boolean wasCorrect,
            int mistakesCount,
            int questionOrder,
            Instant answeredAt) {
        entityManager.persist(
                TestEntities.gameQuestionResultBuilder()
                        .questionResultId(null)
                        .attempt(gameAttempt)
                        .flashcard(flashcard)
                        .questionType(questionType)
                        .questionOrder(questionOrder)
                        .wasCorrect(wasCorrect)
                        .mistakesCount(mistakesCount)
                        .answeredAt(answeredAt)
                        .build());
    }
}
