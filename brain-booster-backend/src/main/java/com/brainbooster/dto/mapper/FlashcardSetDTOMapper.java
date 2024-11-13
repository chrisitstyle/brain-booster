package com.brainbooster.dto.mapper;

import com.brainbooster.dto.FlashcardSetDTO;
import com.brainbooster.model.FlashcardSet;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class FlashcardSetDTOMapper implements Function<FlashcardSet, FlashcardSetDTO> {
    @Override
    public FlashcardSetDTO apply(FlashcardSet flashcardSet) {
        return new FlashcardSetDTO(
                flashcardSet.getSetId(),
                new UserDTOMapper().apply(flashcardSet.getUser()),
                flashcardSet.getSetName(),
                flashcardSet.getDescription(),
                flashcardSet.getCreatedAt()
        );
    }
}
