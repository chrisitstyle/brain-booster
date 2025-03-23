package com.brainbooster.flashcard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/flashcards")
public class FlashcardController {
    private final FlashcardService flashcardService;

    @PostMapping
    public ResponseEntity<Flashcard> addFlashcard(@RequestBody Flashcard flashcard) {

        Flashcard addedFlashcard = flashcardService.addFlashcard(flashcard);
        return new ResponseEntity<>(addedFlashcard, HttpStatus.CREATED);

    }
    @GetMapping
    public ResponseEntity<List<Flashcard>> getAllFlashcards() {
        List<Flashcard> flashcards = flashcardService.getAllFlashcards();
        return new ResponseEntity<>(flashcards, HttpStatus.OK);
    }
    @GetMapping("/{flashcardId}")
    public ResponseEntity<Flashcard> getFlashcardById(@PathVariable Long flashcardId) {

        return ResponseEntity.ok(flashcardService.getFlashcardById(flashcardId));
    }

    @PatchMapping("/{flashcardId}")
    public ResponseEntity<Flashcard> updateFlashcard(@RequestBody Flashcard updatedFlashcard, @PathVariable Long flashcardId) {

        flashcardService.updateFlashcard(updatedFlashcard, flashcardId);
        return new ResponseEntity<>(updatedFlashcard, HttpStatus.OK);
    }

    @DeleteMapping("/{flashcardId}")
    public ResponseEntity<String> deleteFlashcardById(@PathVariable Long flashcardId) {

        flashcardService.deleteFlashcardById(flashcardId);
        return new ResponseEntity<>("Flashcard with id: " + flashcardId + " has been deleted", HttpStatus.OK);
    }

}
