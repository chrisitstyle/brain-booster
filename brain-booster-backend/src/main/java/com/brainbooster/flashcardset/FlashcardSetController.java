package com.brainbooster.flashcardset;

import com.brainbooster.flashcard.Flashcard;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/flashcard-sets")
public class FlashcardSetController {

    private final FlashcardSetService flashcardSetService;

    @PostMapping
    public ResponseEntity<FlashcardSetDTO> addFlashcardSet(@RequestBody FlashcardSet flashcardSet) {

        FlashcardSetDTO savedFlashcardSet = flashcardSetService.addFlashcardSet(flashcardSet);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedFlashcardSet.setId())
                .toUri();

        return ResponseEntity.created(location).body(savedFlashcardSet);

    }

    @GetMapping
    public List<FlashcardSetDTO> getAllFlashcardSets() {

        return flashcardSetService.getAllFlashcardSets();

    }

    @GetMapping("/{setId}")
    public FlashcardSetDTO getFlashcardSetById(@PathVariable long setId) {

        return flashcardSetService.getFlashcardSetById(setId);
    }

    @GetMapping("/{setId}/flashcards")
    public List<Flashcard> getAllFlashcardsInSet(@PathVariable long setId) {
        return flashcardSetService.getAllFlashcardsInSet(setId);
    }

    @PatchMapping("/{setId}")
    public FlashcardSetDTO updateFlashcardSetById(@RequestBody FlashcardSet updatedFlashcardSet, @PathVariable long setId){

       return flashcardSetService.updateFlashcardSet(updatedFlashcardSet, setId);
    }

    @DeleteMapping("/{setId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String deleteFlashcardSetById(@PathVariable Long setId) {
        flashcardSetService.deleteFlashcardSetById(setId);
        return "FlashcardSet with id: " + setId + " has been deleted.";
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        return ex.getMessage();
    }

}