package com.brainbooster.flashcardset;

import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.mapper.FlashcardDTOMapper;
import com.brainbooster.flashcard.starred.UserStarredFlashcardRepository;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetUpdateDTO;
import com.brainbooster.flashcardset.mapper.FlashcardSetCreationDTOMapper;
import com.brainbooster.flashcardset.mapper.FlashcardSetDTOMapper;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import com.brainbooster.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FlashcardSetService {
    private final UserRepository userRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserStarredFlashcardRepository starredFlashcardRepository;
    private final FlashcardSetDTOMapper flashcardSetDTOMapper;
    private final FlashcardDTOMapper flashcardDTOMapper;

    @Transactional
    public FlashcardSetDTO addFlashcardSet(FlashcardSetCreationDTO flashcardSetCreationDTO) {

        // TODO implement feature that assigns flashcard set to authenticated user
        User setOwner = userRepository.findById(flashcardSetCreationDTO.userId())
                .orElseThrow(() -> new NoSuchElementException("User with id: " + flashcardSetCreationDTO.userId() + " not found"));

        FlashcardSet flashcardSet = FlashcardSetCreationDTOMapper.toEntity(flashcardSetCreationDTO);

        flashcardSet.setUser(setOwner);

        FlashcardSet savedFlashcardSet = flashcardSetRepository.save(flashcardSet);

        return flashcardSetDTOMapper.apply(savedFlashcardSet);
    }

    public List<FlashcardSetDTO> getAllFlashcardSets() {
        return flashcardSetRepository.findAllWithUsers()
                .stream()
                .map(flashcardSetDTOMapper)
                .toList();
    }

    public FlashcardSetDTO getFlashcardSetById(Long setId) {
        return flashcardSetRepository.findByIdWithUser(setId)
                .map(flashcardSetDTOMapper)
                .orElseThrow(() -> new NoSuchElementException("FlashcardSet with id: " + setId + " not found"));
    }

    public List<FlashcardDTO> getAllFlashcardsInSet(Long setId) {

        if (!flashcardSetRepository.existsById(setId)) {
            throw new NoSuchElementException("FlashcardSet with id: " + setId + " not found");
        }

        User authUser = SecurityUtils.getAuthenticatedUserOrNull();

        Set<Long> starredFlashcardIds = authUser == null
                ? Set.of()
                : starredFlashcardRepository.findStarredFlashcardIdsByUserIdAndSetId(
                authUser.getUserId(),
                setId
        );

        return flashcardRepository.findAllByFlashcardSet_SetId(setId)
                .stream()
                .map(flashcard -> flashcardDTOMapper.toDto(
                        flashcard,
                        starredFlashcardIds.contains(flashcard.getFlashcardId())
                ))
                .toList();
    }

    @Transactional
    public FlashcardSetDTO updateFlashcardSet(FlashcardSetUpdateDTO updateDTO, Long setId) {
        FlashcardSet existingSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new NoSuchElementException("FlashcardSet with id: " + setId + " not found"));

        // verify if the user is admin or owner of set
        verifySetAccess(existingSet, "You are not allowed to edit this flashcard set!");

        existingSet.setSetName(updateDTO.setName());
        existingSet.setDescription(updateDTO.description());

        flashcardSetRepository.save(existingSet);
        return flashcardSetDTOMapper.apply(existingSet);
    }

    public void deleteFlashcardSetById(Long setId) {

        FlashcardSet existingSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new NoSuchElementException("FlashcardSet with id: " + setId + " not found"));

        verifySetAccess(existingSet, "You are not allowed to delete this flashcard set!");

        flashcardSetRepository.delete(existingSet);
    }

    /**
     * Helper method to verify if the authenticated user has rights to modify/delete the set.
     */
    private void verifySetAccess(FlashcardSet flashcardSet, String errorMessage) {
        User authUser = SecurityUtils.getAuthenticatedUser();
        boolean isAdmin = authUser.getRole().equals(Role.ADMIN);
        boolean isOwner = flashcardSet.getUser().getUserId().equals(authUser.getUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
