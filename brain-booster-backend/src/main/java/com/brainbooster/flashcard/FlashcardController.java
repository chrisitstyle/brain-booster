package com.brainbooster.flashcard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/flashcards")
public class FlashcardController {
    private final FlashcardService flashcardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Flashcard addFlashcard(@RequestBody Flashcard flashcard) {

        return flashcardService.addFlashcard(flashcard);


    }

    @GetMapping
    public List<Flashcard> getAllFlashcards() {
        return flashcardService.getAllFlashcards();

    }

    @GetMapping("/{flashcardId}")
    public Flashcard getFlashcardById(@PathVariable long flashcardId) {

        return flashcardService.getFlashcardById(flashcardId);
    }

    @PatchMapping("/{flashcardId}")
    public Flashcard updateFlashcard(@RequestBody Flashcard updatedFlashcard, @PathVariable long flashcardId) {

        return flashcardService.updateFlashcard(updatedFlashcard, flashcardId);

    }

    @DeleteMapping("/{flashcardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String deleteFlashcardById(@PathVariable long flashcardId) {

        flashcardService.deleteFlashcardById(flashcardId);
        return "Flashcard with id: " + flashcardId + " has been deleted";
    }
}
