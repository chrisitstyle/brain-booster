package com.brainbooster.gameresult.attempt;

import com.brainbooster.gameresult.GameMode;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameAttemptRepository extends JpaRepository<GameAttempt, Long> {

    @EntityGraph(attributePaths = {"user", "set"})
    @Query("""
            SELECT attempt
            FROM GameAttempt attempt
            WHERE attempt.user.userId = :userId
              AND (:setId IS NULL OR attempt.set.setId = :setId)
              AND (:mode IS NULL OR attempt.mode = :mode)
              AND (:fromDateTime IS NULL OR attempt.completedAt >= :fromDateTime)
              AND (:toDateTimeExclusive IS NULL OR attempt.completedAt < :toDateTimeExclusive)
            """)
    Page<GameAttempt> findByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("mode") GameMode mode,
            @Param("fromDateTime") Instant fromDateTime,
            @Param("toDateTimeExclusive") Instant toDateTimeExclusive,
            Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = {"user", "set"})
    @NonNull
    Optional<GameAttempt> findById(@NonNull Long attemptId);

    @EntityGraph(attributePaths = {"user",
            "set",
            "questionResults",
            "questionResults.flashcard"})
    Optional<GameAttempt> findWithQuestionResultsByAttemptId(Long attemptId);

    @EntityGraph(attributePaths = {"user", "set"})
    @Query("""
            SELECT attempt
            FROM GameAttempt attempt
            WHERE attempt.user.userId = :userId
              AND attempt.set.setId = :setId
            ORDER BY attempt.completedAt ASC
            """)
    List<GameAttempt> findByUserIdAndSetIdOrderByCompletedAtAsc(
            @Param("userId") Long userId,
            @Param("setId") Long setId
    );
}