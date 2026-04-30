package com.brainbooster.flashcard;

import com.brainbooster.flashcard.dto.FlashcardCreationDTO;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.dto.FlashcardUpdateDTO;
import jakarta.validation.Valid;
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
    public FlashcardDTO addFlashcard(@Valid @RequestBody FlashcardCreationDTO flashcardCreationDTO) {

        return flashcardService.addFlashcard(flashcardCreationDTO);
    }

    @GetMapping
    public List<FlashcardDTO> getAllFlashcards() {
        return flashcardService.getAllFlashcards();

    }

    @GetMapping("/{flashcardId}")
    public FlashcardDTO getFlashcardById(@PathVariable Long flashcardId) {

        return flashcardService.getFlashcardById(flashcardId);
    }

    @PatchMapping("/{flashcardId}")
    public FlashcardDTO updateFlashcard(@Valid @RequestBody FlashcardUpdateDTO updatedFlashcard, @PathVariable Long flashcardId) {

        return flashcardService.updateFlashcard(updatedFlashcard, flashcardId);

    }

    @DeleteMapping("/{flashcardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String deleteFlashcardById(@PathVariable Long flashcardId) {

        flashcardService.deleteFlashcardById(flashcardId);
        return "Flashcard with id: " + flashcardId + " has been deleted";
    }
}
