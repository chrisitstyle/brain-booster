package com.brainbooster.flashcardset;

import com.brainbooster.user.UserDTOMapper;
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
