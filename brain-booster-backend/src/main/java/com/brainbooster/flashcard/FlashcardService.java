package com.brainbooster.flashcard;

import com.brainbooster.flashcard.dto.FlashcardCreationDTO;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.dto.FlashcardUpdateDTO;
import com.brainbooster.flashcard.mapper.FlashcardDTOMapper;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardDTOMapper flashcardDTOMapper;

    @Transactional
    public FlashcardDTO addFlashcard(FlashcardCreationDTO flashcardCreationDTO) {
        FlashcardSet flashcardSetFromDB = flashcardSetRepository.findById(flashcardCreationDTO.setId())
                .orElseThrow(() -> new NoSuchElementException("FlashcardSet with id " + flashcardCreationDTO.setId() + " not found"));


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
                .orElseThrow(() -> new NoSuchElementException("Flashcard with id " + flashcardId + " not found"));
    }

    @Transactional
    public FlashcardDTO updateFlashcard(FlashcardUpdateDTO updatedFlashcard, Long flashcardId) {

        Flashcard existingFlashcardFromDB = flashcardRepository.findByIdWithSetAndUser(flashcardId)
                .orElseThrow(() -> new NoSuchElementException("Flashcard with id " + flashcardId + " not found"));

        verifyFlashcardSetAccess(existingFlashcardFromDB.getFlashcardSet(), "You are not allowed to edit this flashcard!");

        existingFlashcardFromDB.setTerm(updatedFlashcard.term());
        existingFlashcardFromDB.setDefinition(updatedFlashcard.definition());

        Flashcard savedFlashcard = flashcardRepository.save(existingFlashcardFromDB);
        return flashcardDTOMapper.apply(savedFlashcard);
    }

    public void deleteFlashcardById(Long flashcardId) {

        Flashcard existingFlashcard = flashcardRepository.findByIdWithSetAndUser(flashcardId)
                .orElseThrow(() -> new NoSuchElementException("Flashcard with id " + flashcardId + " not found"));


        verifyFlashcardSetAccess(existingFlashcard.getFlashcardSet(), "You are not allowed to delete this flashcard!");
        flashcardRepository.delete(existingFlashcard);
    }


    private void verifyFlashcardSetAccess(FlashcardSet flashcardSetFromDB, String errorMessage) {

        User authUser = SecurityUtils.getAuthenticatedUser();

        // actual permission verification (admin or owner)
        boolean isAdmin = authUser.getRole().equals(Role.ADMIN);
        boolean isOwner = flashcardSetFromDB.getUser().getUserId().equals(authUser.getUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
