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

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FolderService {

    private static final String NOT_FOUND_MSG_SUFFIX = " not found";
    private static final String FOLDER_WITH_ID_MSG_PREFIX = "Folder with id: ";
    private static final String FLASHCARD_SET_WITH_ID_MSG_PREFIX = "FlashcardSet with id: ";
    private static final String EDIT_FOLDER_ACCESS_DENIED_MSG = "You are not allowed to edit this folder!";
    private static final String DELETE_FOLDER_ACCESS_DENIED_MSG = "You are not allowed to delete this folder!";
    private static final String ADD_SET_TO_FOLDER_ACCESS_DENIED_MSG = "You cannot add this set to your folder!";

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
                .createdAt(Instant.now())
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
                .orElseThrow(() -> new NoSuchElementException(buildFolderNotFoundMessage(folderId)));

        return folderDTOMapper.apply(folder);
    }

    @Transactional
    public FolderDTO updateFolder(Long folderId, FolderUpdateDTO dto) {
        Folder folder = folderRepository.findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new NoSuchElementException(buildFolderNotFoundMessage(folderId)));

        verifyFolderAccess(folder, EDIT_FOLDER_ACCESS_DENIED_MSG);

        folder.setName(dto.name());
        folder.setDescription(dto.description());
        return folderDTOMapper.apply(folder);
    }

    @Transactional
    public void deleteFolder(Long folderId) {
        Folder folder = folderRepository.findByIdWithUser(folderId)
                .orElseThrow(() -> new NoSuchElementException(buildFolderNotFoundMessage(folderId)));

        verifyFolderAccess(folder, DELETE_FOLDER_ACCESS_DENIED_MSG);

        folderRepository.delete(folder);
    }

    @Transactional
    public FolderDTO addSetToFolder(Long folderId, Long setId) {
        Folder folder = folderRepository.findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new NoSuchElementException(buildFolderNotFoundMessage(folderId)));

        verifyFolderAccess(folder, EDIT_FOLDER_ACCESS_DENIED_MSG);

        FlashcardSet flashcardSet = flashcardSetRepository.findByIdWithUser(setId)
                .orElseThrow(() -> new NoSuchElementException(buildFlashcardSetNotFoundMessage(setId)));

        verifySetCanBeAdded(flashcardSet);

        folder.getFlashcardSets().add(flashcardSet);

        return reloadFolderDTO(folderId);
    }

    @Transactional
    public void removeSetFromFolder(Long folderId, Long setId) {
        Folder folder = folderRepository.findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new NoSuchElementException(buildFolderNotFoundMessage(folderId)));

        verifyFolderAccess(folder, EDIT_FOLDER_ACCESS_DENIED_MSG);

        if (!flashcardSetRepository.existsById(setId)) {
            throw new NoSuchElementException(buildFlashcardSetNotFoundMessage(setId));
        }

        boolean removed = folder.getFlashcardSets()
                .removeIf(flashcardSet -> flashcardSet.getSetId().equals(setId));

        if (!removed) {
            throw new NoSuchElementException(buildFlashcardSetNotInFolderMessage(setId, folderId));
        }
    }

    private FolderDTO reloadFolderDTO(Long folderId) {
        entityManager.flush();
        entityManager.clear();

        Folder refreshedFolder = folderRepository.findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new NoSuchElementException(buildFolderNotFoundMessage(folderId)));

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
            throw new AccessDeniedException(ADD_SET_TO_FOLDER_ACCESS_DENIED_MSG);
        }
    }

    private String buildFolderNotFoundMessage(Long folderId) {
        return FOLDER_WITH_ID_MSG_PREFIX + folderId + NOT_FOUND_MSG_SUFFIX;
    }

    private String buildFlashcardSetNotFoundMessage(Long setId) {
        return FLASHCARD_SET_WITH_ID_MSG_PREFIX + setId + NOT_FOUND_MSG_SUFFIX;
    }

    private String buildFlashcardSetNotInFolderMessage(Long setId, Long folderId) {
        return FLASHCARD_SET_WITH_ID_MSG_PREFIX + setId + " is not in folder with id: " + folderId;
    }
}