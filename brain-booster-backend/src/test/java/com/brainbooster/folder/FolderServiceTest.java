package com.brainbooster.folder;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.folder.dto.FolderCreationDTO;
import com.brainbooster.folder.dto.FolderDTO;
import com.brainbooster.folder.dto.FolderUpdateDTO;
import com.brainbooster.folder.mapper.FolderDTOMapper;
import com.brainbooster.user.User;
import com.brainbooster.utils.SecurityUtils;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.brainbooster.utils.TestEntities.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private FlashcardSetRepository flashcardSetRepository;

    @Mock
    private FolderDTOMapper folderDTOMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private FolderService folderService;

    @Test
    @DisplayName("createFolder should save folder for authenticated user")
    void createFolder_shouldSaveFolderAndReturnDTO() {
        User authUser = createUser();
        FolderCreationDTO creationDTO = createFolderCreationDTO();
        Folder savedFolder = createFolder();
        FolderDTO expectedDTO = createEmptyFolderDTO();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(authUser);
            when(folderRepository.save(any(Folder.class))).thenReturn(savedFolder);
            when(folderDTOMapper.apply(savedFolder)).thenReturn(expectedDTO);

            FolderDTO result = folderService.createFolder(creationDTO);

            assertThat(result).isEqualTo(expectedDTO);

            ArgumentCaptor<Folder> folderCaptor = ArgumentCaptor.forClass(Folder.class);
            verify(folderRepository).save(folderCaptor.capture());

            Folder capturedFolder = folderCaptor.getValue();
            assertThat(capturedFolder.getUser()).isEqualTo(authUser);
            assertThat(capturedFolder.getName()).isEqualTo("test_folder_name");
            assertThat(capturedFolder.getDescription()).isEqualTo("test_folder_description");
            assertThat(capturedFolder.getCreatedAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("getAllFolders should map all folders to DTOs")
    void getAllFolders_shouldReturnMappedFolders() {
        Folder folder = createFolder();
        FolderDTO dto = createEmptyFolderDTO();

        when(folderRepository.findAllWithSetsAndUser()).thenReturn(List.of(folder));
        when(folderDTOMapper.apply(folder)).thenReturn(dto);

        List<FolderDTO> result = folderService.getAllFolders();

        assertThat(result).containsExactly(dto);
        verify(folderRepository).findAllWithSetsAndUser();
    }

    @Test
    @DisplayName("getMyFolders should use authenticated user's id")
    void getMyFolders_shouldReturnAuthenticatedUserFolders() {
        User authUser = createUser();
        Folder folder = createFolder();
        FolderDTO dto = createEmptyFolderDTO();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(authUser);
            when(folderRepository.findAllByUserId(1L)).thenReturn(List.of(folder));
            when(folderDTOMapper.apply(folder)).thenReturn(dto);

            List<FolderDTO> result = folderService.getMyFolders();

            assertThat(result).containsExactly(dto);
            verify(folderRepository).findAllByUserId(1L);
        }
    }

    @Test
    @DisplayName("getFoldersByNickname should return folders for nickname")
    void getFoldersByNickname_shouldReturnFolders() {
        Folder folder = createFolder();
        FolderDTO dto = createEmptyFolderDTO();

        when(folderRepository.findAllByUserNickname("johndoe")).thenReturn(List.of(folder));
        when(folderDTOMapper.apply(folder)).thenReturn(dto);

        List<FolderDTO> result = folderService.getFoldersByNickname("johndoe");

        assertThat(result).containsExactly(dto);
        verify(folderRepository).findAllByUserNickname("johndoe");
    }


    @Test
    @DisplayName("getFolderById should throw when folder does not exist")
    void getFolderById_whenFolderDoesNotExist_shouldThrow() {
        when(folderRepository.findByIdWithSetsAndUser(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.getFolderById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Folder with id: 999 not found");
    }

    @Test
    @DisplayName("updateFolder should update folder fields")
    void updateFolder_shouldUpdateFieldsAndReturnDTO() {
        User authUser = createUser();
        Folder folder = createFolder();
        FolderUpdateDTO updateDTO = createFolderUpdateDTO();
        FolderDTO expectedDTO = new FolderDTO(
                1L,
                "johndoe",
                "Updated Folder",
                "Updated folder description",
                0L,
                List.of()
        );

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(authUser);
            when(folderRepository.findByIdWithSetsAndUser(1L)).thenReturn(Optional.of(folder));
            when(folderDTOMapper.apply(folder)).thenReturn(expectedDTO);

            FolderDTO result = folderService.updateFolder(1L, updateDTO);

            assertThat(result).isEqualTo(expectedDTO);
            assertThat(folder.getName()).isEqualTo("Updated Folder");
            assertThat(folder.getDescription()).isEqualTo("Updated folder description");
        }
    }

    @Test
    @DisplayName("updateFolder should throw when name is blank")
    void updateFolder_whenNameIsBlank_shouldThrow() {
        User authUser = createUser();
        Folder folder = createFolder();
        FolderUpdateDTO updateDTO = new FolderUpdateDTO("   ", "description");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(authUser);
            when(folderRepository.findByIdWithSetsAndUser(1L)).thenReturn(Optional.of(folder));

            assertThatThrownBy(() -> folderService.updateFolder(1L, updateDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Folder name cannot be empty");
        }
    }

    @Test
    @DisplayName("deleteFolder should delete folder when authenticated user is owner")
    void deleteFolder_whenOwner_shouldDeleteFolder() {
        User authUser = createUser();
        Folder folder = createFolder();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(authUser);
            when(folderRepository.findByIdWithUser(1L)).thenReturn(Optional.of(folder));

            folderService.deleteFolder(1L);

            verify(folderRepository).delete(folder);
        }
    }

    @Test
    @DisplayName("addSetToFolder should add set and reload folder DTO")
    void addSetToFolder_shouldAddSetAndReturnReloadedDTO() {
        User authUser = createUser();
        Folder folder = createFolder();
        folder.setFlashcardSets(new HashSet<>());

        FlashcardSet flashcardSet = createFlashcardSet();
        Folder refreshedFolder = createFolderWithFlashcardSet();
        FolderDTO expectedDTO = createFolderDTO();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(authUser);
            when(folderRepository.findByIdWithSetsAndUser(1L))
                    .thenReturn(Optional.of(folder))
                    .thenReturn(Optional.of(refreshedFolder));
            when(flashcardSetRepository.findByIdWithUser(1L)).thenReturn(Optional.of(flashcardSet));
            when(folderDTOMapper.apply(refreshedFolder)).thenReturn(expectedDTO);

            FolderDTO result = folderService.addSetToFolder(1L, 1L);

            assertThat(result).isEqualTo(expectedDTO);
            assertThat(folder.getFlashcardSets()).contains(flashcardSet);
            verify(entityManager).flush();
            verify(entityManager).clear();
        }
    }

    @Test
    @DisplayName("removeSetFromFolder should remove set from folder")
    void removeSetFromFolder_shouldRemoveSet() {
        User authUser = createUser();
        Folder folder = createFolderWithFlashcardSet();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(authUser);
            when(folderRepository.findByIdWithSetsAndUser(1L)).thenReturn(Optional.of(folder));
            when(flashcardSetRepository.existsById(1L)).thenReturn(true);

            folderService.removeSetFromFolder(1L, 1L);

            assertThat(folder.getFlashcardSets()).isEmpty();
        }
    }

    @Test
    @DisplayName("removeSetFromFolder should throw when set does not exist")
    void removeSetFromFolder_whenSetDoesNotExist_shouldThrow() {
        User authUser = createUser();
        Folder folder = createFolder();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(authUser);
            when(folderRepository.findByIdWithSetsAndUser(1L)).thenReturn(Optional.of(folder));
            when(flashcardSetRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> folderService.removeSetFromFolder(1L, 999L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("FlashcardSet with id: 999 not found");

            verify(flashcardSetRepository).existsById(999L);
        }
    }

    @Test
    @DisplayName("removeSetFromFolder should throw when set is not in folder")
    void removeSetFromFolder_whenSetIsNotInFolder_shouldThrow() {
        User authUser = createUser();
        Folder folder = createFolder();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(authUser);
            when(folderRepository.findByIdWithSetsAndUser(1L)).thenReturn(Optional.of(folder));
            when(flashcardSetRepository.existsById(1L)).thenReturn(true);

            assertThatThrownBy(() -> folderService.removeSetFromFolder(1L, 1L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("FlashcardSet with id: 1 is not in folder with id: 1");
        }
    }
}

