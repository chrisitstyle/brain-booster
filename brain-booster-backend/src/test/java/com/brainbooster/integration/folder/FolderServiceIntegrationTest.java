package com.brainbooster.integration.folder;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.folder.Folder;
import com.brainbooster.folder.FolderRepository;
import com.brainbooster.folder.FolderService;
import com.brainbooster.folder.dto.FolderCreationDTO;
import com.brainbooster.folder.dto.FolderDTO;
import com.brainbooster.folder.dto.FolderUpdateDTO;
import com.brainbooster.integration.AbstractIntegrationTest;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Sql(scripts = "/insert-it-test-users.sql")
class FolderServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FolderService folderService;
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
    @Autowired
    private UserRepository userRepository;
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // -- CREATE TESTS --

    @Test
    @DisplayName("createFolder - Should save and return FolderDTO for authenticated user")
    void createFolder_ShouldSaveFolder() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        FolderCreationDTO dto = new FolderCreationDTO("New Folder", "New Description");

        // when
        FolderDTO result = folderService.createFolder(dto);

        // then
        assertThat(result.name()).isEqualTo("New Folder");
        assertThat(result.description()).isEqualTo("New Description");
        assertThat(result.nickname()).isEqualTo(owner.getNickname());
    }

    // -- READ TESTS --

    @Test
    @DisplayName("getAllFolders - Should return all folders from database")
    void getAllFolders_ShouldReturnList() {
        // given
        User user1 = userRepository.findById(1L).orElseThrow();
        User user2 = userRepository.findById(2L).orElseThrow();

        folderRepository.save(TestEntities.folderBuilder().folderId(null).user(user1).build());
        folderRepository.save(TestEntities.folderBuilder().folderId(null).user(user2).build());

        // when
        List<FolderDTO> results = folderService.getAllFolders();

        // then
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("getMyFolders - Should return folders strictly owned by authenticated user")
    void getMyFolders_ShouldReturnUserFolders() {
        // given
        User adminUser = userRepository.findById(1L).orElseThrow();
        User regularUser = userRepository.findById(2L).orElseThrow();

        mockAuthenticatedUser(regularUser);

        folderRepository.save(TestEntities.folderBuilder().folderId(null).user(adminUser).name("Admin's folder").build());
        folderRepository.save(TestEntities.folderBuilder().folderId(null).user(regularUser).name("User's folder").build());

        // when
        List<FolderDTO> results = folderService.getMyFolders();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("User's folder");
    }

    @Test
    @DisplayName("getFoldersByNickname - Should return folders for specific nickname")
    void getFoldersByNickname_ShouldReturnList() {
        // given
        User targetUser = userRepository.findById(2L).orElseThrow();
        folderRepository.save(TestEntities.folderBuilder().folderId(null).user(targetUser).build());

        // when
        List<FolderDTO> results = folderService.getFoldersByNickname(targetUser.getNickname());

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().nickname()).isEqualTo(targetUser.getNickname());
    }

    @Test
    @DisplayName("getFolderById - Should throw NoSuchElementException when folder does not exist")
    void getFolderById_ShouldThrowNoSuchElementException() {
        // when, then
        assertThatThrownBy(() -> folderService.getFolderById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Folder with id: 999 not found");
    }

    // -- UPDATE TESTS --

    @Test
    @DisplayName("updateFolder - Should modify folder when called by owner")
    void updateFolder_ShouldModifyFolder() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        Folder savedFolder = folderRepository.save(TestEntities.folderBuilder()
                .folderId(null)
                .user(owner)
                .name("Old Name").build());
        FolderUpdateDTO updateDTO = new FolderUpdateDTO("Updated Name", "Updated Desc");

        // when
        FolderDTO result = folderService.updateFolder(savedFolder.getFolderId(), updateDTO);

        // then
        assertThat(result.name()).isEqualTo("Updated Name");
        assertThat(result.description()).isEqualTo("Updated Desc");
    }

    @Test
    @DisplayName("updateFolder - Should throw AccessDeniedException when called by non-owner")
    void updateFolder_ShouldThrowAccessDeniedException_WhenNotOwner() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        User anotherUser = userRepository.findById(3L).orElseThrow();
        mockAuthenticatedUser(anotherUser);

        Folder savedFolder = folderRepository.save(TestEntities
                .folderBuilder()
                .folderId(null)
                .user(owner).build());

        FolderUpdateDTO updateDTO = new FolderUpdateDTO("Hacked Name", "Hacked Desc");

        Long folderId = savedFolder.getFolderId();

        // when, then
        assertThatThrownBy(() -> folderService.updateFolder(folderId, updateDTO))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not allowed to edit this folder!");
    }

    // -- DELETE TESTS --

    @Test
    @DisplayName("deleteFolder - Should remove folder when called by owner")
    void deleteFolder_ShouldRemoveFromDB() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        Folder savedFolder = folderRepository.save(TestEntities
                .folderBuilder()
                .folderId(null)
                .user(owner).build());

        Long folderId = savedFolder.getFolderId();

        // when
        folderService.deleteFolder(folderId);

        // then
        assertThat(folderRepository.findById(folderId)).isEmpty();
    }

    @Test
    @DisplayName("deleteFolder - Should throw AccessDeniedException when called by non-owner")
    void deleteFolder_ShouldThrowAccessDeniedException_WhenNotOwner() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        User anotherUser = userRepository.findById(3L).orElseThrow();
        mockAuthenticatedUser(anotherUser);

        Folder savedFolder = folderRepository.save(TestEntities
                .folderBuilder()
                .folderId(null)
                .user(owner).build());

        Long folderId = savedFolder.getFolderId();

        // when, then
        assertThatThrownBy(() -> folderService.deleteFolder(folderId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not allowed to delete this folder!");
    }

    // -- FOLDER-SET RELATION TESTS --

    @Test
    @DisplayName("addSetToFolder - Should link set to folder when user owns both")
    void addSetToFolder_ShouldCreateRelation() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        Folder savedFolder = folderRepository.save(
                TestEntities
                        .folderBuilder()
                        .folderId(null)
                        .user(owner).build());

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        // when
        FolderDTO result = folderService.addSetToFolder(savedFolder.getFolderId(), savedSet.getSetId());

        // then
        assertThat(result.flashcardSets()).hasSize(1);
        assertThat(result.setCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("addSetToFolder - Should throw AccessDeniedException when user does not own the flashcard set")
    void addSetToFolder_ShouldThrowAccessDenied_WhenNotSetOwner() {
        // given
        User folderOwner = userRepository.findById(2L).orElseThrow();
        User setOwner = userRepository.findById(3L).orElseThrow();
        mockAuthenticatedUser(folderOwner);

        Folder savedFolder = folderRepository.save(
                TestEntities
                        .folderBuilder()
                        .folderId(null)
                        .user(folderOwner).build());

        FlashcardSet foreignSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(setOwner).build());

        Long folderId = savedFolder.getFolderId();
        Long setId = foreignSet.getSetId();

        // when, then
        assertThatThrownBy(() -> folderService.addSetToFolder(folderId, setId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You cannot add this set to your folder!");
    }

    @Test
    @DisplayName("removeSetFromFolder - Should unlink set from folder when user is owner")
    void removeSetFromFolder_ShouldDeleteRelation() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        Folder folder = TestEntities
                .folderBuilder()
                .folderId(null)
                .user(owner).build();

        FlashcardSet set = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        folder.getFlashcardSets().add(set);
        Folder savedFolder = folderRepository.save(folder);

        Long folderId = savedFolder.getFolderId();
        Long setId = set.getSetId();

        // when
        folderService.removeSetFromFolder(folderId, setId);

        // then
        Folder refreshedFolder = folderRepository.findByIdWithSetsAndUser(folderId).orElseThrow();
        assertThat(refreshedFolder.getFlashcardSets()).isEmpty();
    }

    @Test
    @DisplayName("removeSetFromFolder - Should throw NoSuchElementException when set is not inside the folder")
    void removeSetFromFolder_ShouldThrowNoSuchElementException_WhenNotInFolder() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        Folder savedFolder = folderRepository.save(
                TestEntities
                        .folderBuilder()
                        .folderId(null)
                        .user(owner).build());

        FlashcardSet standaloneSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        Long folderId = savedFolder.getFolderId();
        Long setId = standaloneSet.getSetId();

        // when, then
        assertThatThrownBy(() -> folderService.removeSetFromFolder(folderId, setId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("is not in folder with id:");
    }
}
