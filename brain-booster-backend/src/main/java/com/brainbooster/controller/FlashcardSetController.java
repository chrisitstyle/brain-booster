package com.brainbooster.controller;

import com.brainbooster.model.FlashcardSet;
import com.brainbooster.service.FlashcardSetService;
import lombok.RequiredArgsConstructor;
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
        return flashcardSetService.addFlashcardSet(flashcardSet);
    }

    @GetMapping
    public ResponseEntity<List<FlashcardSet>> getAllFlashcardSets() {
        List<FlashcardSet> flashcardSets = flashcardSetService.getAllFlashcardSets();
        return ResponseEntity.ok(flashcardSets);
    }
}