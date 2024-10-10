package com.brainbooster.repository;

import com.brainbooster.model.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {
}
