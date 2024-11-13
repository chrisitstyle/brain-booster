package com.brainbooster.repository;

import com.brainbooster.model.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {

    @Query("SELECT fs FROM FlashcardSet fs WHERE fs.user.userId = :userId")
    List<FlashcardSet> findByUserId(Long userId);

}
