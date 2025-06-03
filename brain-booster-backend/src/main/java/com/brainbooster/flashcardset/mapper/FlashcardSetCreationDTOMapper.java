package com.brainbooster.flashcardset.mapper;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.user.User;

import java.time.LocalDateTime;

public class FlashcardSetCreationDTOMapper {

    private FlashcardSetCreationDTOMapper() {
        throw new IllegalStateException("Utility class");
    }


    public static FlashcardSet toEntity(FlashcardSetCreationDTO dto){

        FlashcardSet flashcardSet = new FlashcardSet();
        User user = new User();
        user.setUserId(dto.userId());
        flashcardSet.setUser(user);

        flashcardSet.setSetName(dto.setName());
        flashcardSet.setDescription(dto.description());
        flashcardSet.setCreatedAt(LocalDateTime.now());

        return flashcardSet;

    }

    public static FlashcardSetCreationDTO toDTO(FlashcardSet flashcardSet) {
        return new FlashcardSetCreationDTO(
               flashcardSet.getUser().getUserId(),
                flashcardSet.getSetName(),
                flashcardSet.getDescription()
        );
    }

}
