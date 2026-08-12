package com.brainbooster.folder;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.folder.dto.FolderCreationDTO;
import com.brainbooster.folder.dto.FolderDTO;
import com.brainbooster.folder.dto.FolderUpdateDTO;
import com.brainbooster.folder.mapper.FolderDTOMapper;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.security.authorization.OwnerOrAdminPolicy;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private static final String NOT_FOUND_MSG_SUFFIX = " not found";
    private static final String FOLDER_WITH_ID_MSG_PREFIX = "Folder with id: ";
    private static final String FLASHCARD_SET_WITH_ID_MSG_PREFIX = "FlashcardSet with id: ";
    private static final String EDIT_FOLDER_ACCESS_DENIED_MSG = "You are not allowed to edit this folder!";
    private static final String DELETE_FOLDER_ACCESS_DENIED_MSG = "You are not allowed to delete this folder!";
    private static final String ADD_FLASHCARD_SET_TO_FOLDER_ACCESS_DENIED_MSG = "You cannot add this set to your folder!";

    private final FolderRepository folderRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FolderDTOMapper folderDTOMapper;
    private final OwnerOrAdminPolicy ownerOrAdminPolicy;
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;

    @Transactional
    public FolderDTO createFolder(FolderCreationDTO dto) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        User userReference = userRepository.getReferenceById(authenticatedUser.userId());


        Folder folder = Folder.builder()
                .user(userReference)
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
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        return folderRepository.findAllByUserId(authenticatedUser.userId())
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
        Folder folder = folderRepository
                .findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new ResourceNotFoundException(buildFolderNotFoundMessage(folderId)));

        return folderDTOMapper.apply(folder);
    }

    @Transactional
    public FolderDTO updateFolder(Long folderId, FolderUpdateDTO dto) {
        Folder folder = folderRepository
                .findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new ResourceNotFoundException(buildFolderNotFoundMessage(folderId)));

        verifyFolderAccess(folder, EDIT_FOLDER_ACCESS_DENIED_MSG);

        folder.setName(dto.name());
        folder.setDescription(dto.description());
        return folderDTOMapper.apply(folder);
    }

    @Transactional
    public void deleteFolder(Long folderId) {
        Folder folder = folderRepository.findByIdWithUser(folderId)
                .orElseThrow(() -> new ResourceNotFoundException(buildFolderNotFoundMessage(folderId)));

        verifyFolderAccess(folder, DELETE_FOLDER_ACCESS_DENIED_MSG);

        folderRepository.delete(folder);
    }

    @Transactional
    public FolderDTO addFlashcardSetToFolder(
            Long folderId,
            Long flashcardSetId
    ) {
        Folder folder = folderRepository.findByIdWithSetsAndUser(folderId).orElseThrow(
                        () -> new ResourceNotFoundException(buildFolderNotFoundMessage(folderId)));

        verifyFolderAccess(folder, EDIT_FOLDER_ACCESS_DENIED_MSG);

        FlashcardSet flashcardSet = flashcardSetRepository
                .findByIdWithUser(flashcardSetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                                buildFlashcardSetNotFoundMessage(flashcardSetId)));

        verifyFlashcardSetCanBeAdded(flashcardSet);

        folder.addFlashcardSet(flashcardSet);

        return folderDTOMapper.apply(folder);
    }

    @Transactional
    public void removeFlashcardSetFromFolder(Long folderId, Long flashcardSetId) {
        Folder folder = folderRepository.findByIdWithSetsAndUser(folderId)
                .orElseThrow(() -> new ResourceNotFoundException(buildFolderNotFoundMessage(folderId)));

        verifyFolderAccess(folder, EDIT_FOLDER_ACCESS_DENIED_MSG);

        if (!flashcardSetRepository.existsById(flashcardSetId)) {
            throw new ResourceNotFoundException(
                    buildFlashcardSetNotFoundMessage(flashcardSetId));
        }

        boolean removed = folder.removeFlashcardSet(flashcardSetId);

        if (!removed) {
            throw new ResourceNotFoundException(
                    buildFlashcardSetNotInFolderMessage(flashcardSetId, folderId));
        }
    }

    private void verifyFolderAccess(Folder folder, String errorMessage) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        ownerOrAdminPolicy.verify(
                authenticatedUser,
                folder.getUser().getUserId(),
                errorMessage);
    }

    private void verifyFlashcardSetCanBeAdded(FlashcardSet flashcardSet) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();
        ownerOrAdminPolicy.verify(
                authenticatedUser,
                flashcardSet.getUser().getUserId(),
                ADD_FLASHCARD_SET_TO_FOLDER_ACCESS_DENIED_MSG
        );
    }

    private String buildFolderNotFoundMessage(Long folderId) {
        return FOLDER_WITH_ID_MSG_PREFIX + folderId + NOT_FOUND_MSG_SUFFIX;
    }

    private String buildFlashcardSetNotFoundMessage(Long flashcardSetId) {
        return FLASHCARD_SET_WITH_ID_MSG_PREFIX + flashcardSetId + NOT_FOUND_MSG_SUFFIX;
    }

    private String buildFlashcardSetNotInFolderMessage(Long flashcardSetId, Long folderId) {
        return FLASHCARD_SET_WITH_ID_MSG_PREFIX + flashcardSetId + " is not in folder with id: " + folderId;
    }
}