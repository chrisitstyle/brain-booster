package com.brainbooster.spacedrepetition.state;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
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

}
