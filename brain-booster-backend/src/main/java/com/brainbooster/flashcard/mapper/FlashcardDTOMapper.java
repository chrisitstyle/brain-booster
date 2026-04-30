package com.brainbooster.flashcard.mapper;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import java.util.function.Function;
import org.springframework.stereotype.Component;
@Component
public class FlashcardDTOMapper implements Function<Flashcard, FlashcardDTO> {
    @Override
    public FlashcardDTO apply(Flashcard flashcard) {
        return new FlashcardDTO(
                flashcard.getFlashcardId(),
                flashcard.getFlashcardSet() != null ? flashcard.getFlashcardSet().getSetId() : null,
                flashcard.getTerm(),
                flashcard.getDefinition()
        );

    }
}
