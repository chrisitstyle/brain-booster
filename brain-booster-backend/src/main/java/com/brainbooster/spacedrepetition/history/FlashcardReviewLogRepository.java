package com.brainbooster.spacedrepetition.history;

import com.brainbooster.spacedrepetition.state.ReviewStage;
import com.brainbooster.spacedrepetition.stats.ReviewActivityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlashcardReviewLogRepository extends JpaRepository<FlashcardReviewLog, Long> {

    Optional<FlashcardReviewLog> findByRequestId(UUID requestId);

    List<FlashcardReviewLog> findByReviewState_User_UserIdOrderByReviewedAtDesc(Long userId);


    /**
     * Counts new cards introduced during the user's local study day.
     */
    @Query("""
        SELECT COUNT(reviewLog)
        FROM FlashcardReviewLog reviewLog
        WHERE reviewLog.reviewState.user.userId = :userId
          AND reviewLog.stageBefore = :newStage
          AND reviewLog.reviewedAt >= :dayStart
          AND reviewLog.reviewedAt < :dayEnd
        """)
    long countNewCardsReviewedDuringDay(
            @Param("userId") Long userId,
            @Param("newStage") ReviewStage newStage,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd);

    /**
     * Counts reviews of previously introduced cards during a study day.
     */
    @Query("""
        SELECT COUNT(reviewLog)
        FROM FlashcardReviewLog reviewLog
        WHERE reviewLog.reviewState.user.userId = :userId
          AND reviewLog.stageBefore <> :newStage
          AND reviewLog.reviewedAt >= :dayStart
          AND reviewLog.reviewedAt < :dayEnd
        """)
    long countExistingCardsReviewedDuringDay(
            @Param("userId") Long userId,
            @Param("newStage") ReviewStage newStage,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd);


    @Query("""
        SELECT COUNT(reviewLog)
        FROM FlashcardReviewLog reviewLog
        WHERE reviewLog.reviewState.user.userId = :userId
          AND reviewLog.stageBefore = :stage
          AND reviewLog.reviewedAt >= :startInclusive
          AND reviewLog.reviewedAt < :endExclusive
        """)
    long countByUserStageAndPeriod(
            @Param("userId") Long userId,
            @Param("stage") ReviewStage stage,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive);

    @Query("""
        SELECT new com.brainbooster.spacedrepetition.stats.ReviewActivityEvent(
            reviewLog.reviewedAt,
            reviewLog.rating,
            reviewLog.stageBefore
        )
        FROM FlashcardReviewLog reviewLog
        WHERE reviewLog.reviewState.user.userId = :userId
          AND reviewLog.reviewedAt >= :startInclusive
          AND reviewLog.reviewedAt <= :currentTime
        ORDER BY reviewLog.reviewedAt
        """)
    List<ReviewActivityEvent> findActivityEvents(
            @Param("userId") Long userId,
            @Param("startInclusive") Instant startInclusive,
            @Param("currentTime") Instant currentTime);


}
