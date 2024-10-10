package com.brainbooster.service;

import com.brainbooster.model.FlashcardSet;
import com.brainbooster.repository.FlashcardSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardSetService {
    private final FlashcardSetRepository flashcardSetRepository;

    public ResponseEntity<FlashcardSet> addFlashcardSet(FlashcardSet flashcardSet) {

        FlashcardSet flashcardSetSaved = flashcardSetRepository.save(flashcardSet);
        return ResponseEntity.status(HttpStatus.CREATED).body(flashcardSetSaved);
    }

    public List<FlashcardSet> getAllFlashcardSets() {
        return flashcardSetRepository.findAll();
    }
}
