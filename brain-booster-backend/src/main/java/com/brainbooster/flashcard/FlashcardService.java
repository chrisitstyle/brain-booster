package com.brainbooster.flashcard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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
    public Flashcard getFlashcardById(Long flashcardId) {

        return flashcardRepository.findById(flashcardId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard with id " + flashcardId + " not found"));
    }

    public Flashcard updateFlashcard(Flashcard updatedFlashcard, Long flashcardId) {

        return flashcardRepository.findById(flashcardId)
                .map(flashcard -> {
                    flashcard.setTerm(updatedFlashcard.getTerm());
                    flashcard.setDefinition(updatedFlashcard.getDefinition());
                    return flashcardRepository.save(flashcard);
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard with id " + flashcardId + " not found"));
    }

    public void deleteFlashcardById(Long flashcardId) {
        Optional<Flashcard> flashcardExists = flashcardRepository.findById(flashcardId);
        if (flashcardExists.isPresent()) {
            flashcardRepository.deleteById(flashcardId);
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard with id " + flashcardId + " not found");
        }
    }
}
