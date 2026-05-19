package com.brainbooster.folder;

import com.brainbooster.folder.dto.FolderCreationDTO;
import com.brainbooster.folder.dto.FolderDTO;
import com.brainbooster.folder.dto.FolderUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/folders")
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderDTO createFolder(@Valid @RequestBody FolderCreationDTO folderCreationDTO) {
        return folderService.createFolder(folderCreationDTO);
    }

    @GetMapping
    public List<FolderDTO> getAllFolders() {
        return folderService.getAllFolders();
    }

    @GetMapping("/me")
    public List<FolderDTO> getMyFolders() {
        return folderService.getMyFolders();
    }

    @GetMapping("/{folderId}")
    public FolderDTO getFolderById(@PathVariable Long folderId) {
        return folderService.getFolderById(folderId);
    }


    @PatchMapping("/{folderId}")
    public FolderDTO updateFolder(
            @PathVariable Long folderId,
            @Valid @RequestBody FolderUpdateDTO folderUpdateDTO
    ) {
        return folderService.updateFolder(folderId, folderUpdateDTO);
    }

    @DeleteMapping("/{folderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(@PathVariable Long folderId) {
        folderService.deleteFolder(folderId);
    }

    @PostMapping("/{folderId}/sets/{setId}")
    public FolderDTO addSetToFolder(
            @PathVariable Long folderId,
            @PathVariable Long setId
    ) {
        return folderService.addSetToFolder(folderId, setId);
    }

    @DeleteMapping("/{folderId}/sets/{setId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSetFromFolder(
            @PathVariable Long folderId,
            @PathVariable Long setId
    ) {
        folderService.removeSetFromFolder(folderId, setId);
    }
}
