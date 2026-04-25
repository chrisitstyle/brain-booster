package com.brainbooster.flashcardset.mapper;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.user.dto.UserSummaryDTO;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class FlashcardSetDTOMapper implements Function<FlashcardSet, FlashcardSetDTO> {
    @Override
    public FlashcardSetDTO apply(FlashcardSet flashcardSet) {
        return new FlashcardSetDTO(
                flashcardSet.getSetId(),
                new UserSummaryDTO(flashcardSet.getUser().getNickname()),
                flashcardSet.getSetName(),
                flashcardSet.getDescription(),
                flashcardSet.getCreatedAt(),
                flashcardSet.getTermCount()
        );
    }
}
