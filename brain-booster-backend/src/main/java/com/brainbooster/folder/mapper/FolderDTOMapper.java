package com.brainbooster.folder.mapper;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.folder.Folder;
import com.brainbooster.folder.dto.FlashcardSetInFolderDTO;
import com.brainbooster.folder.dto.FolderDTO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Component
public class FolderDTOMapper implements Function<Folder, FolderDTO> {

    @Override
    public FolderDTO apply(Folder folder) {
        return new FolderDTO(
                folder.getFolderId(),
                folder.getUser().getNickname(),
                folder.getName(),
                folder.getDescription(),
                folder.getSetCount(),
                mapFlashcardSets(folder.getFlashcardSets())
        );
    }

    private List<FlashcardSetInFolderDTO> mapFlashcardSets(Set<FlashcardSet> flashcardSets) {
        return flashcardSets.stream()
                .sorted(Comparator.comparing(FlashcardSet::getCreatedAt).reversed())
                .map(this::mapFlashcardSet)
                .toList();
    }

    private FlashcardSetInFolderDTO mapFlashcardSet(FlashcardSet flashcardSet) {
        return new FlashcardSetInFolderDTO(
                flashcardSet.getSetId(),
                flashcardSet.getSetName(),
                flashcardSet.getTermCount()
        );
    }

}