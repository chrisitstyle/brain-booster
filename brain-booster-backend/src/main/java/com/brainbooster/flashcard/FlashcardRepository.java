package com.brainbooster.flashcard;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findAllByFlashcardSet_SetId(Long setId);

    @Query("SELECT f FROM Flashcard f JOIN FETCH f.flashcardSet fs JOIN FETCH fs.user WHERE f.flashcardId = :flashcardId")
    Optional<Flashcard> findByIdWithSetAndUser(@Param("flashcardId") Long flashcardId);


    /**
     * Returns flashcards that the specified user has never reviewed.
     *
     * <p>The pageable limit prevents loading an entire large set
     * when only a small number of new cards is required.
     */
    @Query("""
        SELECT flashcard
        FROM Flashcard flashcard
        WHERE flashcard.flashcardSet.setId = :setId
          AND NOT EXISTS (
              SELECT reviewState.reviewStateId
              FROM FlashcardReviewState reviewState
              WHERE reviewState.flashcard = flashcard
                AND reviewState.user.userId = :userId
          )
        ORDER BY flashcard.flashcardId ASC
        """)
    List<Flashcard> findUnreviewedBySetIdAndUserId(
            @Param("setId") Long setId,
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("""
        SELECT COUNT(flashcard)
        FROM Flashcard flashcard
        WHERE flashcard.flashcardSet.user.userId = :userId
        """)
    long countOwnedFlashcards(@Param("userId") Long userId);

}
