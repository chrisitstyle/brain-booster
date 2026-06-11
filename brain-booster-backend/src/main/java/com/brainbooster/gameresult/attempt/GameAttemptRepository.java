package com.brainbooster.gameresult.attempt;

import com.brainbooster.gameresult.GameMode;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameAttemptRepository extends JpaRepository<GameAttempt, Long> {

    @EntityGraph(attributePaths = {"user", "set"})
    List<GameAttempt> findByUser_UserIdOrderByCompletedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "set"})
    List<GameAttempt> findByUser_UserIdAndSet_SetIdOrderByCompletedAtDesc(
            Long userId,
            Long setId
    );

    @EntityGraph(attributePaths = {"user", "set"})
    List<GameAttempt> findByUser_UserIdAndSet_SetIdAndModeOrderByCompletedAtDesc(
            Long userId,
            Long setId,
            GameMode mode
    );

    @Override
    @EntityGraph(attributePaths = {"user", "set"})
    Optional<GameAttempt> findById(@NonNull Long attemptId);

    @EntityGraph(attributePaths = {"user",
            "set",
            "questionResults",
            "questionResults.flashcard"})
    Optional<GameAttempt> findWithQuestionResultsByAttemptId(Long attemptId);
}