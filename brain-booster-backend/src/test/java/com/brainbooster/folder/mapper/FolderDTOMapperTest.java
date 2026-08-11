package com.brainbooster.folder.mapper;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.folder.Folder;
import com.brainbooster.folder.dto.FlashcardSetInFolderDTO;
import com.brainbooster.folder.dto.FolderDTO;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import static com.brainbooster.utils.TestEntities.createFolder;
import static com.brainbooster.utils.TestEntities.flashcardSetBuilder;
import static org.assertj.core.api.Assertions.assertThat;

class FolderDTOMapperTest {

    private final FolderDTOMapper folderDTOMapper =
            new FolderDTOMapper();

    @Test
    void apply_ShouldMapFolderAndCalculateFlashcardSetCount() {
        // given
        Folder folder = createFolder();

        FlashcardSet firstSet = flashcardSetBuilder()
                .setId(1L)
                .setName("First set")
                .termCount(5L)
                .createdAt(Instant.parse("2026-08-01T10:00:00Z"))
                .build();

        FlashcardSet secondSet = flashcardSetBuilder()
                .setId(2L)
                .setName("Second set")
                .termCount(10L)
                .createdAt(Instant.parse("2026-08-02T10:00:00Z"))
                .build();

        folder.addFlashcardSet(firstSet);
        folder.addFlashcardSet(secondSet);

        // when
        FolderDTO result = folderDTOMapper.apply(folder);

        // then
        assertThat(result.folderId()).isEqualTo(folder.getFolderId());
        assertThat(result.nickname())
                .isEqualTo(folder.getUser().getNickname());
        assertThat(result.name()).isEqualTo(folder.getName());
        assertThat(result.description())
                .isEqualTo(folder.getDescription());

        assertThat(result.setCount()).isEqualTo(2L);

        assertThat(result.flashcardSets()).hasSize(2);
    }

    @Test
    void apply_ShouldMapFlashcardSetsNewestFirst() {
        // given
        Folder folder = createFolder();

        FlashcardSet olderSet = flashcardSetBuilder()
                .setId(1L)
                .setName("Older set")
                .termCount(5L)
                .createdAt(Instant.parse("2026-08-01T10:00:00Z"))
                .build();

        FlashcardSet newerSet = flashcardSetBuilder()
                .setId(2L)
                .setName("Newer set")
                .termCount(10L)
                .createdAt(Instant.parse("2026-08-02T10:00:00Z"))
                .build();

        folder.setFlashcardSets(
                new HashSet<>(List.of(olderSet, newerSet))
        );

        // when
        FolderDTO result = folderDTOMapper.apply(folder);

        // then
        assertThat(result.flashcardSets())
                .containsExactly(
                        new FlashcardSetInFolderDTO(
                                2L,
                                "Newer set",
                                10L
                        ),
                        new FlashcardSetInFolderDTO(
                                1L,
                                "Older set",
                                5L
                        )
                );
    }

    @Test
    void apply_ShouldReturnZeroCount_WhenFolderHasNoFlashcardSets() {
        // given
        Folder folder = createFolder();

        // when
        FolderDTO result = folderDTOMapper.apply(folder);

        // then
        assertThat(result.setCount()).isZero();
        assertThat(result.flashcardSets()).isEmpty();
    }
}
