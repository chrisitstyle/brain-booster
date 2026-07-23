package com.brainbooster.flashcard;

import com.brainbooster.flashcard.dto.FlashcardCreationDTO;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.dto.FlashcardUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Flashcards", description = "Endpoints for managing flashcards")
@RestController
@RequiredArgsConstructor
@RequestMapping("/flashcards")
public class FlashcardController {
    private final FlashcardService flashcardService;


    @Operation(
            summary = "Create a new flashcard",
            description = "Creates a new flashcard for the currently authenticated user (owner of set).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Flashcard created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to access this resource")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FlashcardDTO addFlashcard(@Valid @RequestBody FlashcardCreationDTO flashcardCreationDTO) {

        return flashcardService.addFlashcard(flashcardCreationDTO);
    }

    @Operation(
            summary = "Get all flashcards",
            description = "Fetches all flashcards from the database."
    )
    @ApiResponse(responseCode = "200", description = "Flashcards fetched successfully")
    @GetMapping
    public List<FlashcardDTO> getAllFlashcards() {
        return flashcardService.getAllFlashcards();

    }

    @Operation(
            summary = "Get flashcard by ID",
            description = "Fetches a single flashcard by its ID."
    )
    @ApiResponse(responseCode = "200", description = "Flashcard fetched successfully")
    @ApiResponse(responseCode = "404", description = "Flashcard not found")
    @GetMapping("/{flashcardId}")
    public FlashcardDTO getFlashcardById(
            @Parameter(description = "ID of the flashcard", example = "1")
            @PathVariable Long flashcardId) {

        return flashcardService.getFlashcardById(flashcardId);
    }

    @Operation(
            summary = "Update flashcard",
            description = "Updates an existing flashcard by its ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Flashcard updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to update this flashcard")
    @ApiResponse(responseCode = "404", description = "Flashcard not found")
    @PatchMapping("/{flashcardId}")
    public FlashcardDTO updateFlashcard(@Valid @RequestBody FlashcardUpdateDTO updatedFlashcard,
                                        @Parameter(description = "ID of the flashcard", example = "1")
                                        @PathVariable Long flashcardId) {

        return flashcardService.updateFlashcard(updatedFlashcard, flashcardId);

    }

    @Operation(
            summary = "Star flashcard",
            description = "Marks a flashcard as starred for the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Flashcard starred successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "404", description = "Flashcard not found")
    @PostMapping("/{flashcardId}/starred")
    public FlashcardDTO starFlashcard(@PathVariable Long flashcardId) {
        return flashcardService.starFlashcard(flashcardId);
    }

    @Operation(
            summary = "Unstar flashcard",
            description = "Removes starred status from a flashcard for the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Flashcard unstarred successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "404", description = "Flashcard not found")
    @DeleteMapping("/{flashcardId}/starred")
    public FlashcardDTO unstarFlashcard(@PathVariable Long flashcardId) {
        return flashcardService.unstarFlashcard(flashcardId);
    }

    @Operation(
            summary = "Delete flashcard",
            description = "Deletes an existing flashcard by its ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "Flashcard deleted successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to delete this flashcard")
    @ApiResponse(responseCode = "404", description = "Flashcard not found")
    @DeleteMapping("/{flashcardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String deleteFlashcardById(
            @Parameter(description = "ID of the flashcard", example = "1")
            @PathVariable Long flashcardId) {

        flashcardService.deleteFlashcardById(flashcardId);
        return "Flashcard with id: " + flashcardId + " has been deleted";
    }
}
