package com.brainbooster.flashcard;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcard.dto.FlashcardCreationDTO;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.dto.FlashcardUpdateDTO;
import com.brainbooster.flashcard.mapper.FlashcardDTOMapper;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.security.authorization.OwnerOrAdminPolicy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FlashcardService {

    private static final String NOT_FOUND_MESSAGE_SUFFIX = " not found";
    private static final String FLASHCARD_WITH_ID_MESSAGE_PREFIX = "Flashcard with id ";
    private static final String FLASHCARD_SET_WITH_ID_MESSAGE_PREFIX = "FlashcardSet with id ";

    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardDTOMapper flashcardDTOMapper;
    private final OwnerOrAdminPolicy ownerOrAdminPolicy;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public FlashcardDTO addFlashcard(FlashcardCreationDTO flashcardCreationDTO) {
        FlashcardSet flashcardSetFromDB = flashcardSetRepository.findById(flashcardCreationDTO.setId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        FLASHCARD_SET_WITH_ID_MESSAGE_PREFIX + flashcardCreationDTO.setId() + NOT_FOUND_MESSAGE_SUFFIX
                ));

        verifyFlashcardSetAccess(flashcardSetFromDB, "You can only add flashcards to your own sets!");

        Flashcard flashcardToSave = Flashcard.builder()
                .flashcardSet(flashcardSetFromDB)
                .term(flashcardCreationDTO.term())
                .definition(flashcardCreationDTO.definition())
                .build();

        Flashcard savedFlashcard = flashcardRepository.save(flashcardToSave);
        return flashcardDTOMapper.apply(savedFlashcard);
    }

    public List<FlashcardDTO> getAllFlashcards() {
        return flashcardRepository.findAll()
                .stream()
                .map(flashcardDTOMapper)
                .toList();
    }

    public FlashcardDTO getFlashcardById(Long flashcardId) {
        return flashcardRepository.findById(flashcardId)
                .map(flashcardDTOMapper)
                .orElseThrow(() -> new ResourceNotFoundException(buildFlashcardNotFoundMessage(flashcardId)));
    }

    @Transactional
    public FlashcardDTO updateFlashcard(FlashcardUpdateDTO updatedFlashcard, Long flashcardId) {
        Flashcard existingFlashcardFromDB = flashcardRepository.findByIdWithSetAndUser(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException(buildFlashcardNotFoundMessage(flashcardId)));

        verifyFlashcardSetAccess(existingFlashcardFromDB.getFlashcardSet(), "You are not allowed to edit this flashcard!");

        existingFlashcardFromDB.setTerm(updatedFlashcard.term());
        existingFlashcardFromDB.setDefinition(updatedFlashcard.definition());

        Flashcard savedFlashcard = flashcardRepository.save(existingFlashcardFromDB);
        return flashcardDTOMapper.apply(savedFlashcard);
    }


    public void deleteFlashcardById(Long flashcardId) {
        Flashcard existingFlashcard = flashcardRepository.findByIdWithSetAndUser(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException(buildFlashcardNotFoundMessage(flashcardId)));

        verifyFlashcardSetAccess(existingFlashcard.getFlashcardSet(), "You are not allowed to delete this flashcard!");
        flashcardRepository.delete(existingFlashcard);
    }

    private void verifyFlashcardSetAccess(FlashcardSet flashcardSetFromDB, String errorMessage) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        ownerOrAdminPolicy.verify(
                authenticatedUser,
                flashcardSetFromDB.getUser().getUserId(),
                errorMessage);
    }

    private String buildFlashcardNotFoundMessage(Long flashcardId) {
        return FLASHCARD_WITH_ID_MESSAGE_PREFIX + flashcardId + NOT_FOUND_MESSAGE_SUFFIX;
    }
}