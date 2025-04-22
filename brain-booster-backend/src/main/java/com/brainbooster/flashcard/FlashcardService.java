package com.brainbooster.flashcard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;

    public Flashcard addFlashcard(Flashcard flashcard) {
        return flashcardRepository.save(flashcard);
    }
    public List<Flashcard> getAllFlashcards() {
        return flashcardRepository.findAll();
    }
    public Flashcard getFlashcardById(long flashcardId) {

        return flashcardRepository.findById(flashcardId).orElseThrow(() ->
                new NoSuchElementException("Flashcard with id " + flashcardId + " not found"));
    }

    public Flashcard updateFlashcard(Flashcard updatedFlashcard, long flashcardId) {

        Flashcard existingFlashcard =  flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new NoSuchElementException("Flashcard with id " + flashcardId + " not found"));

        existingFlashcard.setTerm(updatedFlashcard.getTerm());
        existingFlashcard.setDefinition(updatedFlashcard.getDefinition());

        return flashcardRepository.save(existingFlashcard);

    }

    public void deleteFlashcardById(long flashcardId) {

        if(!flashcardRepository.existsById(flashcardId)) {
            throw new NoSuchElementException("Flashcard with id " + flashcardId + " not found");
        }
        flashcardRepository.deleteById(flashcardId);
    }
}
