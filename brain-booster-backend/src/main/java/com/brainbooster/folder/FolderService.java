package com.brainbooster.folder;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.folder.dto.FolderCreationDTO;
import com.brainbooster.folder.dto.FolderDTO;
import com.brainbooster.folder.dto.FolderUpdateDTO;
import com.brainbooster.folder.mapper.FolderDTOMapper;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.utils.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FolderDTOMapper folderDTOMapper;
    private final EntityManager entityManager;

    @Transactional
    public FolderDTO createFolder(FolderCreationDTO dto) {
        User authUser = SecurityUtils.getAuthenticatedUser();

        Folder folder = Folder.builder()
                .user(authUser)
                .name(dto.name())
                .description(dto.description())
                .createdAt(LocalDateTime.now())
                .build();

        Folder savedFolder = folderRepository.save(folder);

        return folderDTOMapper.apply(savedFolder);
    }

    public List<FolderDTO> getAllFolders() {

        return folderRepository.findAllWithSetsAndUser()
                .stream()
                .map(folderDTOMapper)
                .toList();
    }

    public List<FolderDTO> getMyFolders() {
        User authUser = SecurityUtils.getAuthenticatedUser();

        return folderRepository.findAllByUserId(authUser.getUserId())
                .stream()
                .map(folderDTOMapper)
                .toList();
    }

    public List<FolderDTO> getFoldersByNickname(String nickname) {

        return folderRepository.findAllByUserNickname(nickname)
                .stream()
                .map(folderDTOMapper)
                .toList();
    }

    public FolderDTO getFolderById(Long folderId) {
        Folder folder = folderRepository.findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new NoSuchElementException("Folder with id: " + folderId + " not found"));

        return folderDTOMapper.apply(folder);
    }

    @Transactional
    public FolderDTO updateFolder(Long folderId, FolderUpdateDTO dto) {
        Folder folder = folderRepository.findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new NoSuchElementException("Folder with id: " + folderId + " not found"));

        verifyFolderAccess(folder, "You are not allowed to edit this folder!");

        folder.setName(dto.name());
        folder.setDescription(dto.description());
        return folderDTOMapper.apply(folder);
    }

    @Transactional
    public void deleteFolder(Long folderId) {
        Folder folder = folderRepository.findByIdWithUser(folderId)
                .orElseThrow(() -> new NoSuchElementException("Folder with id: " + folderId + " not found"));

        verifyFolderAccess(folder, "You are not allowed to delete this folder!");

        folderRepository.delete(folder);
    }

    @Transactional
    public FolderDTO addSetToFolder(Long folderId, Long setId) {
        Folder folder = folderRepository.findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new NoSuchElementException("Folder with id: " + folderId + " not found"));

        verifyFolderAccess(folder, "You are not allowed to edit this folder!");

        FlashcardSet flashcardSet = flashcardSetRepository.findByIdWithUser(setId)
                .orElseThrow(() -> new NoSuchElementException("FlashcardSet with id: " + setId + " not found"));

        verifySetCanBeAdded(flashcardSet);

        folder.getFlashcardSets().add(flashcardSet);

        return reloadFolderDTO(folderId);
    }

    @Transactional
    public void removeSetFromFolder(Long folderId, Long setId) {
        Folder folder = folderRepository.findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new NoSuchElementException("Folder with id: " + folderId + " not found"));

        verifyFolderAccess(folder, "You are not allowed to edit this folder!");

        if (!flashcardSetRepository.existsById(setId)) {
            throw new NoSuchElementException("FlashcardSet with id: " + setId + " not found");
        }

        boolean removed = folder.getFlashcardSets()
                .removeIf(flashcardSet -> flashcardSet.getSetId().equals(setId));

        if (!removed) {
            throw new NoSuchElementException(
                    "FlashcardSet with id: " + setId + " is not in folder with id: " + folderId
            );
        }
    }

    private FolderDTO reloadFolderDTO(Long folderId) {
        entityManager.flush();
        entityManager.clear();

        Folder refreshedFolder = folderRepository.findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new NoSuchElementException("Folder with id: " + folderId + " not found"));

        return folderDTOMapper.apply(refreshedFolder);
    }

    private void verifyFolderAccess(Folder folder, String errorMessage) {
        User authUser = SecurityUtils.getAuthenticatedUser();

        boolean isAdmin = authUser.getRole().equals(Role.ADMIN);
        boolean isOwner = folder.getUser().getUserId().equals(authUser.getUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(errorMessage);
        }
    }

    private void verifySetCanBeAdded(FlashcardSet flashcardSet) {
        User authUser = SecurityUtils.getAuthenticatedUser();

        boolean isAdmin = authUser.getRole().equals(Role.ADMIN);
        boolean isSetOwner = flashcardSet.getUser().getUserId().equals(authUser.getUserId());

        if (!isAdmin && !isSetOwner) {
            throw new AccessDeniedException("You cannot add this set to your folder!");
        }
    }
}
