package com.brainbooster.gameresult.questionresult;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameQuestionResultRepository extends JpaRepository<GameQuestionResult, Long> {

    @EntityGraph(attributePaths = {"attempt", "flashcard"})
    List<GameQuestionResult> findByAttempt_AttemptIdOrderByQuestionOrderAsc(Long attemptId);


    @Query("""
            SELECT
                flashcard.flashcardId AS flashcardId,
                flashcard.term AS term,
                flashcard.definition AS definition,
                COUNT(questionResult.questionResultId) AS totalAnswers,
                COUNT(
                    CASE WHEN questionResult.wasCorrect = true
                    THEN 1 END
                ) AS correctAnswers,
                SUM(questionResult.mistakesCount) AS totalMistakes,
                MAX(questionResult.answeredAt) AS lastAnsweredAt
            FROM GameQuestionResult questionResult
            JOIN questionResult.attempt attempt
            JOIN questionResult.flashcard flashcard
            WHERE attempt.user.userId = :userId
              AND attempt.set.setId = :setId
            GROUP BY
                flashcard.flashcardId,
                flashcard.term,
                flashcard.definition
            """)
    List<WeakFlashcardStatisticsProjection> findFlashcardStatisticsByUserIdAndSetId(
            @Param("userId") Long userId,
            @Param("setId") Long setId
    );

    @Query("""
            SELECT
                questionResult.questionType AS questionType,
                COUNT(questionResult.questionResultId) AS totalAnswers,
                COUNT(
                    CASE WHEN questionResult.wasCorrect = true
                    THEN 1 END
                ) AS correctAnswers
            FROM GameQuestionResult questionResult
            JOIN questionResult.attempt attempt
            WHERE attempt.user.userId = :userId
              AND attempt.set.setId = :setId
            GROUP BY questionResult.questionType
            """)
    List<QuestionTypeStatisticsProjection> findQuestionTypeStatisticsByUserIdAndSetId(
            @Param("userId") Long userId,
            @Param("setId") Long setId);

}
