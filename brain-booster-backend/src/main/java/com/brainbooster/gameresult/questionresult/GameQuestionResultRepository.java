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
            SELECT questionResult
            FROM GameQuestionResult questionResult
            JOIN FETCH questionResult.attempt attempt
            JOIN FETCH questionResult.flashcard flashcard
            WHERE attempt.user.userId = :userId
              AND attempt.set.setId = :setId
            ORDER BY questionResult.answeredAt DESC
            """)
    List<GameQuestionResult> findByUserIdAndSetIdOrderByAnsweredAtDesc(
            @Param("userId") Long userId,
            @Param("setId") Long setId
    );
}
