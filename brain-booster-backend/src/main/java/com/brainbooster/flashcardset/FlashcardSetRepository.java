package com.brainbooster.flashcardset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {

    @Query("SELECT fs FROM FlashcardSet fs JOIN FETCH fs.user WHERE fs.user.userId = :userId")
    List<FlashcardSet> findByUserId(Long userId);

    @Query("SELECT fs FROM FlashcardSet fs JOIN FETCH fs.user WHERE fs.setId = :setId")
    Optional<FlashcardSet> findByIdWithUser(Long setId);

    @Query("SELECT fs FROM FlashcardSet fs JOIN FETCH fs.user ORDER BY fs.setId")
    List<FlashcardSet> findAllWithUsers();

}
