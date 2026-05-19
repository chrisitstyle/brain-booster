package com.brainbooster.folder;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FolderRepositoryTest {

    @Autowired
    private FolderRepository folderRepository;

    @Test
    @DisplayName("findAllWithSetsAndUser should return all folders with user and sets")
    void findAllWithSetsAndUser_shouldReturnFoldersWithUserAndSets() {
        List<Folder> folders = folderRepository.findAllWithSetsAndUser();

        assertThat(folders).hasSize(2);
        assertThat(folders)
                .extracting(Folder::getName)
                .containsExactlyInAnyOrder("Johndoe Folder One", "Johndoe Folder Two");

        Folder firstFolder = folders.stream()
                .filter(folder -> folder.getFolderId().equals(1L))
                .findFirst()
                .orElseThrow();

        assertThat(Hibernate.isInitialized(firstFolder.getUser())).isTrue();
        assertThat(Hibernate.isInitialized(firstFolder.getFlashcardSets())).isTrue();
        assertThat(firstFolder.getUser().getNickname()).isEqualTo("johndoe");
        assertThat(firstFolder.getFlashcardSets()).hasSize(2);
        assertThat(firstFolder.getSetCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findAllByUserId should return folders for selected user")
    void findAllByUserId_shouldReturnFoldersForUser() {
        List<Folder> folders = folderRepository.findAllByUserId(2L);

        assertThat(folders).hasSize(2);
        assertThat(folders)
                .extracting(folder -> folder.getUser().getUserId())
                .containsOnly(2L);
    }

    @Test
    @DisplayName("findAllByUserNickname should return folders for selected nickname")
    void findAllByUserNickname_shouldReturnFoldersForNickname() {
        List<Folder> folders = folderRepository.findAllByUserNickname("johndoe");

        assertThat(folders).hasSize(2);
        assertThat(folders)
                .extracting(folder -> folder.getUser().getNickname())
                .containsOnly("johndoe");
    }

    @Test
    @DisplayName("findByIdWithSetsAndUser should return folder with user and sets")
    void findByIdWithSetsAndUser_shouldReturnFolderWithUserAndSets() {
        Optional<Folder> folderOptional = folderRepository.findByIdWithSetsAndUser(1L);

        assertThat(folderOptional).isPresent();

        Folder folder = folderOptional.get();

        assertThat(folder.getFolderId()).isEqualTo(1L);
        assertThat(folder.getName()).isEqualTo("Johndoe Folder One");
        assertThat(folder.getUser().getNickname()).isEqualTo("johndoe");
        assertThat(folder.getFlashcardSets()).hasSize(2);
        assertThat(folder.getSetCount()).isEqualTo(2L);
        assertThat(Hibernate.isInitialized(folder.getUser())).isTrue();
        assertThat(Hibernate.isInitialized(folder.getFlashcardSets())).isTrue();
    }

    @Test
    @DisplayName("findByIdWithUser should return folder with initialized user")
    void findByIdWithUser_shouldReturnFolderWithUser() {
        Optional<Folder> folderOptional = folderRepository.findByIdWithUser(1L);

        assertThat(folderOptional).isPresent();

        Folder folder = folderOptional.get();

        assertThat(folder.getFolderId()).isEqualTo(1L);
        assertThat(folder.getUser().getNickname()).isEqualTo("johndoe");
        assertThat(Hibernate.isInitialized(folder.getUser())).isTrue();
    }

    @Test
    @DisplayName("findByIdWithSetsAndUser should return empty optional for missing folder")
    void findByIdWithSetsAndUser_whenFolderDoesNotExist_shouldReturnEmptyOptional() {
        Optional<Folder> folderOptional = folderRepository.findByIdWithSetsAndUser(999L);

        assertThat(folderOptional).isEmpty();
    }
}

