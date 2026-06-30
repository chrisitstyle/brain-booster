package com.brainbooster.flashcardset.mapper;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;

import java.time.Instant;

public class FlashcardSetCreationDTOMapper {

    private FlashcardSetCreationDTOMapper() {
        throw new IllegalStateException("Utility class");
    }


    public static FlashcardSet toEntity(FlashcardSetCreationDTO dto) {

        FlashcardSet flashcardSet = new FlashcardSet();

        flashcardSet.setSetName(dto.setName());
        flashcardSet.setDescription(dto.description());
        flashcardSet.setCreatedAt(Instant.now());

        return flashcardSet;

    }

}
