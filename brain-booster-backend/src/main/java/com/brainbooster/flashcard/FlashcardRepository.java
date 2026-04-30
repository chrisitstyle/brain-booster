package com.brainbooster.flashcard;

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
}
