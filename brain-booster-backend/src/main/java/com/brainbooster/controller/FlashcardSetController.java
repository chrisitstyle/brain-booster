package com.brainbooster.controller;

import com.brainbooster.dto.FlashcardSetDTO;
import com.brainbooster.model.Flashcard;
import com.brainbooster.model.FlashcardSet;
import com.brainbooster.service.FlashcardSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/flashcardsets")
public class FlashcardSetController {

    private final FlashcardSetService flashcardSetService;

    @PostMapping
    public ResponseEntity<FlashcardSetDTO> addFlashcardSet(@RequestBody FlashcardSet flashcardSet) {

        FlashcardSetDTO savedFlashcardSet = flashcardSetService.addFlashcardSet(flashcardSet);

        return new ResponseEntity<>(savedFlashcardSet, HttpStatus.CREATED);

    }

    @GetMapping
    public ResponseEntity<List<FlashcardSetDTO>> getAllFlashcardSets() {
        List<FlashcardSetDTO> flashcardSets = flashcardSetService.getAllFlashcardSets();
        return ResponseEntity.ok(flashcardSets);
    }

    @GetMapping("/{setId}")
    public ResponseEntity<FlashcardSetDTO> getFlashcardSetById(@PathVariable Long setId) {

        return ResponseEntity.ok(flashcardSetService.getFlashcardSetById(setId));
    }

    @GetMapping("/{setId}/flashcards")
    public ResponseEntity<List<Flashcard>> getAllFlashcardsInSet(@PathVariable Long setId) {
        return ResponseEntity.ok(flashcardSetService.getAllFlashcardsInSet(setId));
    }

    @PatchMapping("/{setId}")
    public ResponseEntity<FlashcardSetDTO> updateFlashcardSetById(@RequestBody FlashcardSet updatedFlashcardSet, @PathVariable Long setId){

        FlashcardSetDTO responseFlashcardSet = flashcardSetService.updateFlashcardSet(updatedFlashcardSet, setId);
        return new ResponseEntity<>(responseFlashcardSet, HttpStatus.OK);
    }

    @DeleteMapping("/{setId}")
    public ResponseEntity<String> deleteFlashcardSetById(@PathVariable Long setId) {
        flashcardSetService.deleteFlashcardSetById(setId);
        return ResponseEntity.ok("FlashcardSet with id: " + setId + " has been deleted.");
    }

}