package com.brainbooster.controller;

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
    public ResponseEntity<FlashcardSet> addFlashcardSet(@RequestBody FlashcardSet flashcardSet) {
        flashcardSetService.addFlashcardSet(flashcardSet);
        return new ResponseEntity<>(flashcardSet, HttpStatus.CREATED);

    }

    @GetMapping
    public ResponseEntity<List<FlashcardSet>> getAllFlashcardSets() {
        List<FlashcardSet> flashcardSets = flashcardSetService.getAllFlashcardSets();
        return ResponseEntity.ok(flashcardSets);
    }

    @GetMapping("/{flashcardSetId}")
    public ResponseEntity<FlashcardSet> getFlashcardSetById(@PathVariable long flashcardSetId) {

        return ResponseEntity.ok(flashcardSetService.getFlashcardSetById(flashcardSetId));
    }

    @PatchMapping("/{flashcardSetId}")
    public ResponseEntity<FlashcardSet> updateFlashcardSetById(@RequestBody FlashcardSet updatedFlashcardSet, @PathVariable Long flashcardSetId){

        flashcardSetService.updateFlashcardSet(updatedFlashcardSet, flashcardSetId);
        return new ResponseEntity<>(updatedFlashcardSet, HttpStatus.OK);
    }

    @DeleteMapping("/{flashcardSetId}")
    public ResponseEntity<String> deleteFlashcardSetById(@PathVariable Long flashcardSetId) {
        flashcardSetService.deleteFlashcardSetById(flashcardSetId);
        return ResponseEntity.ok("FlashcardSet with id: " + flashcardSetId + " has been deleted.");
    }

}