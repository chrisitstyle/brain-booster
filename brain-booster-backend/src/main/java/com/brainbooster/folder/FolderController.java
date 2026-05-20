package com.brainbooster.folder;

import com.brainbooster.folder.dto.FolderCreationDTO;
import com.brainbooster.folder.dto.FolderDTO;
import com.brainbooster.folder.dto.FolderUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Folders", description = "Endpoints for managing user folders")
@RestController
@RequiredArgsConstructor
@RequestMapping("/folders")
public class FolderController {

    private final FolderService folderService;

    @Operation(
            summary = "Create a new folder",
            description = "Creates a new folder for the currently authenticated user."
    )
    @ApiResponse(responseCode = "201", description = "Folder created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to access this resource")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderDTO createFolder(@Valid @RequestBody FolderCreationDTO folderCreationDTO) {
        return folderService.createFolder(folderCreationDTO);
    }

    @Operation(
            summary = "Get all folders",
            description = "Fetches all users' folders from the database."
    )
    @ApiResponse(responseCode = "200", description = "Folders fetched successfully")
    @GetMapping
    public List<FolderDTO> getAllFolders() {
        return folderService.getAllFolders();
    }

    @Operation(
            summary = "Get my folders",
            description = "Fetches folders owned by the currently authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "User folders fetched successfully")
    // TODO   @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to access this resource")
    @GetMapping("/me")
    public List<FolderDTO> getMyFolders() {
        return folderService.getMyFolders();
    }


    @Operation(
            summary = "Get folder by ID",
            description = "Fetches a single folder by its ID."
    )
    @ApiResponse(responseCode = "200", description = "Folder fetched successfully")
    @ApiResponse(responseCode = "404", description = "Folder not found")
    @GetMapping("/{folderId}")
    public FolderDTO getFolderById(
            @Parameter(description = "ID of the folder", example = "1")
            @PathVariable Long folderId) {
        return folderService.getFolderById(folderId);
    }

    @Operation(
            summary = "Update folder",
            description = "Updates an existing folder by its ID."
    )
    @ApiResponse(responseCode = "200", description = "Folder updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to update this folder")
    @ApiResponse(responseCode = "404", description = "Folder not found")
    @PatchMapping("/{folderId}")
    public FolderDTO updateFolder(
            @Parameter(description = "ID of the folder", example = "1")
            @PathVariable Long folderId,
            @Valid @RequestBody FolderUpdateDTO folderUpdateDTO
    ) {
        return folderService.updateFolder(folderId, folderUpdateDTO);
    }

    @Operation(
            summary = "Delete folder",
            description = "Deletes an existing folder by its ID."
    )
    @ApiResponse(responseCode = "204", description = "Folder deleted successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to delete this folder")
    @ApiResponse(responseCode = "404", description = "Folder not found")
    @DeleteMapping("/{folderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(
            @Parameter(description = "ID of the folder", example = "1")
            @PathVariable Long folderId) {
        folderService.deleteFolder(folderId);
    }

    @Operation(
            summary = "Add flashcard set to folder",
            description = "Adds an existing flashcard set to an existing folder."
    )
    @ApiResponse(responseCode = "200", description = "Flashcard set added to folder successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to modify this folder or set")
    @ApiResponse(responseCode = "404", description = "Folder or flashcard set not found")
    @PostMapping("/{folderId}/sets/{setId}")
    public FolderDTO addSetToFolder(
            @Parameter(description = "ID of the  existing folder", example = "1")
            @PathVariable Long folderId,
            @Parameter(description = "ID of the existing flashcard set", example = "10")
            @PathVariable Long setId
    ) {
        return folderService.addSetToFolder(folderId, setId);
    }

    @Operation(
            summary = "Remove flashcard set from folder",
            description = "Removes a flashcard set from a folder."
    )
    @ApiResponse(responseCode = "204", description = "Flashcard set removed from folder successfully")
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
    @ApiResponse(responseCode = "403", description = "User does not have permission to modify this folder or set")
    @ApiResponse(responseCode = "404", description = "Folder or flashcard set not found")
    @DeleteMapping("/{folderId}/sets/{setId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSetFromFolder(
            @Parameter(description = "ID of the folder", example = "1")
            @PathVariable Long folderId,
            @Parameter(description = "ID of the flashcard set", example = "10")
            @PathVariable Long setId
    ) {
        folderService.removeSetFromFolder(folderId, setId);
    }
}
