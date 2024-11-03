package com.brainbooster.service;

import com.brainbooster.model.FlashcardSet;
import com.brainbooster.repository.FlashcardSetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Transactional
    public FlashcardSet updateFlashcardSet(FlashcardSet updatedFlashcardSet, Long flashcardSetId) {

        return flashcardSetRepository.findById(flashcardSetId)
                .map(flashcardSet -> {
                    flashcardSet.setSetName(updatedFlashcardSet.getSetName());
                    flashcardSet.setDescription(updatedFlashcardSet.getDescription());
                    return flashcardSetRepository.save(flashcardSet);
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FlashcardSet with id: " + flashcardSetId + " not found"));
    }

    public void deleteFlashcardSetById(Long flashcardSetId) {

        Optional<FlashcardSet> flashcardSetExists = flashcardSetRepository.findById(flashcardSetId);
        if (flashcardSetExists.isPresent()) {
            flashcardSetRepository.deleteById(flashcardSetId);
        } else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "FlashcardSet with id: " + flashcardSetId + " not found");
        }
    }
}
