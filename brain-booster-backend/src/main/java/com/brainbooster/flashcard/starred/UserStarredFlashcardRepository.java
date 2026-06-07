package com.brainbooster.flashcard.starred;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserStarredFlashcardRepository
        extends JpaRepository<UserStarredFlashcard, UserStarredFlashcardId> {

    boolean existsByUser_UserIdAndFlashcard_FlashcardId(Long userId, Long flashcardId);

    void deleteByUser_UserIdAndFlashcard_FlashcardId(Long userId, Long flashcardId);

    @Query("""
            SELECT usf.flashcard.flashcardId
            FROM UserStarredFlashcard usf
            WHERE usf.user.userId = :userId
            """)
    Set<Long> findStarredFlashcardIdsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT usf.flashcard.flashcardId
            FROM UserStarredFlashcard usf
            WHERE usf.user.userId = :userId
              AND usf.flashcard.flashcardSet.setId = :setId
            """)
    Set<Long> findStarredFlashcardIdsByUserIdAndSetId(
            @Param("userId") Long userId,
            @Param("setId") Long setId
    );
}
