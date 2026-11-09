package com.brainbooster.spacedrepetition.state;

import com.brainbooster.spacedrepetition.stats.ReviewStageCount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FlashcardReviewStateRepository extends JpaRepository<FlashcardReviewState, Long> {

    Optional<FlashcardReviewState> findByUser_UserIdAndFlashcard_FlashcardId(
            Long userId,
            Long flashcardId);

    List<FlashcardReviewState> findByUser_UserIdAndDueAtLessThanEqualOrderByDueAtAsc(
            Long userId,
            Instant dueAt);

    List<FlashcardReviewState> findByUser_UserIdAndFlashcard_FlashcardSet_SetIdAndDueAtLessThanEqualOrderByDueAtAsc(
            Long userId,
            Long setId,
            Instant dueAt);


    /**
     * Returns a limited number of due reviews ordered by due date.
     */
    List<FlashcardReviewState>
    findByUser_UserIdAndFlashcard_FlashcardSet_SetIdAndDueAtLessThanEqualOrderByDueAtAsc(
            Long userId,
            Long setId,
            Instant dueAt,
            Pageable pageable);

    List<FlashcardReviewState> findByUser_UserIdAndFlashcard_FlashcardSet_SetId(
            Long userId,
            Long setId);


    @Query("""
        SELECT new com.brainbooster.spacedrepetition.stats.ReviewStageCount(
            reviewState.state,
            COUNT(reviewState)
        )
        FROM FlashcardReviewState reviewState
        WHERE reviewState.user.userId = :userId
        GROUP BY reviewState.state
        """)
    List<ReviewStageCount> countStagesForUser(
            @Param("userId") Long userId
    );

    @Query("""
        SELECT COUNT(reviewState)
        FROM FlashcardReviewState reviewState
        WHERE reviewState.user.userId = :userId
          AND reviewState.state IN :stages
          AND reviewState.dueAt <= :currentTime
        """)
    long countDueStates(
            @Param("userId") Long userId,
            @Param("stages") Collection<ReviewStage> stages,
            @Param("currentTime") Instant currentTime
    );

    @Query("""
        SELECT reviewState.dueAt
        FROM FlashcardReviewState reviewState
        WHERE reviewState.user.userId = :userId
          AND reviewState.state IN :stages
          AND reviewState.dueAt > :currentTime
          AND reviewState.dueAt < :endExclusive
        """)
    List<Instant> findFutureDueDates(
            @Param("userId") Long userId,
            @Param("stages") Collection<ReviewStage> stages,
            @Param("currentTime") Instant currentTime,
            @Param("endExclusive") Instant endExclusive
    );


}
