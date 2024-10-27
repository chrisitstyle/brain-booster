package com.brainbooster.service;

import com.brainbooster.model.FlashcardSet;
import com.brainbooster.repository.FlashcardSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardSetService {
    private final FlashcardSetRepository flashcardSetRepository;

    public FlashcardSet addFlashcardSet(FlashcardSet flashcardSet) {

        if(flashcardSet.getCreatedAt() == null){
            flashcardSet.setCreatedAt(LocalDateTime.now());
        }
        return flashcardSetRepository.save(flashcardSet);
    }

    public List<FlashcardSet> getAllFlashcardSets() {
        return flashcardSetRepository.findAll();
    }

    public FlashcardSet getFlashcardSetById(long flashcardSetId) {
        return flashcardSetRepository.findById(flashcardSetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FlashcardSet with id: " + flashcardSetId + " not found"));
    }
}
