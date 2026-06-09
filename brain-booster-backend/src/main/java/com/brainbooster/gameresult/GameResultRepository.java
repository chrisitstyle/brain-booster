package com.brainbooster.gameresult;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, Long> {

    @EntityGraph(attributePaths = {"user", "set"})
    Optional<GameResult> findByUser_UserIdAndSet_SetIdAndMode(
            Long userId,
            Long setId,
            GameMode mode
    );

    @EntityGraph(attributePaths = {"user", "set"})
    List<GameResult> findByUser_UserIdOrderByCompletedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "set"})
    List<GameResult> findByUser_UserIdAndSet_SetIdOrderByCompletedAtDesc(
            Long userId,
            Long setId
    );

    @EntityGraph(attributePaths = {"user", "set"})
    List<GameResult> findBySet_SetIdOrderByCompletedAtDesc(Long setId);

    @EntityGraph(attributePaths = {"user", "set"})
    List<GameResult> findAllByOrderByCompletedAtDesc();

    @Override
    @EntityGraph(attributePaths = {"user", "set"})
    Optional<GameResult> findById(@NonNull Long resultId);
}
