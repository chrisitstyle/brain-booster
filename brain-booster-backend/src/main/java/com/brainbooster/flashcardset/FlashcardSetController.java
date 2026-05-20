package com.brainbooster.flashcardset;

import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetUpdateDTO;
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

@Tag(name = "Flashcard Sets", description = "Endpoints for managing flashcard sets")
@RestController
@RequiredArgsConstructor
@RequestMapping("/flashcard-sets")
public class FlashcardSetController {

    private final FlashcardSetService flashcardSetService;

    @Operation(
            summary = "Create a new flashcard set",
            // TODO  description = "Creates a new flashcard set for the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Flashcard set created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to access this resource")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FlashcardSetDTO addFlashcardSet(@Valid @RequestBody FlashcardSetCreationDTO flashcardSetCreationDTO) {

        return flashcardSetService.addFlashcardSet(flashcardSetCreationDTO);
    }

    @Operation(
            summary = "Get all flashcard sets",
            description = "Fetches all public flashcard sets from the database."
    )
    @ApiResponse(responseCode = "200", description = "Flashcard sets fetched successfully")
    @GetMapping
    public List<FlashcardSetDTO> getAllFlashcardSets() {
        return flashcardSetService.getAllFlashcardSets();
    }

    @Operation(
            summary = "Get flashcard set by ID",
            description = "Fetches a single flashcard set by its ID."
    )
    @ApiResponse(responseCode = "200", description = "Flashcard set fetched successfully")
    @ApiResponse(responseCode = "404", description = "Flashcard set not found")
    @GetMapping("/{setId}")
    public FlashcardSetDTO getFlashcardSetById(
            @Parameter(description = "ID of the flashcard set", example = "1")
            @PathVariable Long setId) {

        return flashcardSetService.getFlashcardSetById(setId);
    }

    @Operation(
            summary = "Get all flashcards in a set",
            description = "Fetches all flashcards that belong to a specific flashcard set."
    )
    @ApiResponse(responseCode = "200", description = "Flashcards fetched successfully")
    @ApiResponse(responseCode = "404", description = "Flashcard set not found")
    @GetMapping("/{setId}/flashcards")
    public List<FlashcardDTO> getAllFlashcardsInSet(
            @Parameter(description = "ID of the flashcard set", example = "1")
            @PathVariable Long setId) {
        return flashcardSetService.getAllFlashcardsInSet(setId);
    }

    @Operation(
            summary = "Update flashcard set",
            description = "Updates an existing flashcard set by its ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Flashcard set updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to update this flashcard set")
    @ApiResponse(responseCode = "404", description = "Flashcard set not found")
    @PatchMapping("/{setId}")
    public FlashcardSetDTO updateFlashcardSetById(@Valid @RequestBody FlashcardSetUpdateDTO updatedFlashcardSet,
                                                  @Parameter(description = "ID of the flashcard set", example = "1")
                                                  @PathVariable Long setId) {

        return flashcardSetService.updateFlashcardSet(updatedFlashcardSet, setId);
    }

    @Operation(
            summary = "Delete flashcard set",
            description = "Deletes an existing flashcard set by its ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "Flashcard set deleted successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to delete this flashcard set")
    @ApiResponse(responseCode = "404", description = "Flashcard set not found")
    @DeleteMapping("/{setId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String deleteFlashcardSetById(
            @Parameter(description = "ID of the flashcard set", example = "1")
            @PathVariable Long setId) {
        flashcardSetService.deleteFlashcardSetById(setId);
        return "FlashcardSet with id: " + setId + " has been deleted.";
    }
}