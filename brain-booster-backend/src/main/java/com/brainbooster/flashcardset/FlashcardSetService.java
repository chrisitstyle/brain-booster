package com.brainbooster.flashcardset;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.mapper.FlashcardDTOMapper;
import com.brainbooster.flashcard.starred.UserStarredFlashcardRepository;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetUpdateDTO;
import com.brainbooster.flashcardset.mapper.FlashcardSetCreationDTOMapper;
import com.brainbooster.flashcardset.mapper.FlashcardSetDTOMapper;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.security.authorization.OwnerOrAdminPolicy;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FlashcardSetService {

    private static final String USER_WITH_NICKNAME_MESSAGE_PREFIX = "User with nickname: ";
    private static final String NOT_FOUND_MESSAGE_SUFFIX = " not found";
    private static final String USER_WITH_ID_MESSAGE_PREFIX = "User with id: ";
    private static final String FLASHCARD_SET_WITH_ID_MESSAGE_PREFIX = "FlashcardSet with id: ";
    private static final String EDIT_FLASHCARD_SET_ACCESS_DENIED_MESSAGE =
            "You are not allowed to edit this flashcard set!";
    private static final String DELETE_FLASHCARD_SET_ACCESS_DENIED_MESSAGE =
            "You are not allowed to delete this flashcard set!";

    private final UserRepository userRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserStarredFlashcardRepository starredFlashcardRepository;
    private final FlashcardSetDTOMapper flashcardSetDTOMapper;
    private final FlashcardDTOMapper flashcardDTOMapper;
    private final OwnerOrAdminPolicy ownerOrAdminPolicy;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public FlashcardSetDTO addFlashcardSet(FlashcardSetCreationDTO flashcardSetCreationDTO) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        User setOwner = userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        buildUserNotFoundMessage(authenticatedUser.userId())));

        FlashcardSet flashcardSet = FlashcardSetCreationDTOMapper.toEntity(flashcardSetCreationDTO);

        flashcardSet.setUser(setOwner);

        FlashcardSet savedFlashcardSet = flashcardSetRepository.save(flashcardSet);

        List<Flashcard> flashcards = flashcardSetCreationDTO.flashcards()
                .stream()
                .map(flashcardDTO -> Flashcard.builder()
                        .flashcardSet(savedFlashcardSet)
                        .term(flashcardDTO.term())
                        .definition(flashcardDTO.definition())
                        .build())
                .toList();

        flashcardRepository.saveAll(flashcards);

        return flashcardSetDTOMapper.apply(savedFlashcardSet);
    }

    public List<FlashcardSetDTO> getAllFlashcardSets() {
        return flashcardSetRepository.findAllWithUsers()
                .stream()
                .map(flashcardSetDTOMapper)
                .toList();
    }

    public List<FlashcardSetDTO> getAllFlashcardSetsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    buildUserNotFoundMessage(userId)
            );
        }

        return flashcardSetRepository.findByUserId(userId)
                .stream()
                .map(flashcardSetDTOMapper)
                .toList();
    }

    public List<FlashcardSetDTO> getAllFlashcardSetsByUserNickname(String nickname) {
        if (!userRepository.existsByNickname(nickname)) {
            throw new ResourceNotFoundException(
                    buildUserNotFoundMessage(nickname)
            );
        }

        return flashcardSetRepository.findAllByUserNickname(nickname)
                .stream()
                .map(flashcardSetDTOMapper)
                .toList();
    }

    public FlashcardSetDTO getFlashcardSetById(Long setId) {
        return flashcardSetRepository.findByIdWithUser(setId)
                .map(flashcardSetDTOMapper)
                .orElseThrow(() -> new ResourceNotFoundException(buildFlashcardSetNotFoundMessage(setId)));
    }

    public List<FlashcardDTO> getAllFlashcardsInSet(Long setId) {

        if (!flashcardSetRepository.existsById(setId)) {
            throw new ResourceNotFoundException(buildFlashcardSetNotFoundMessage(setId));
        }

        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUserOrNull();
        Set<Long> starredFlashcardIds = authenticatedUser == null
                ? Set.of()
                : starredFlashcardRepository.findStarredFlashcardIdsByUserIdAndSetId(
                authenticatedUser.userId(),
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
                .orElseThrow(() -> new ResourceNotFoundException(buildFlashcardSetNotFoundMessage(setId)));

        // verify if the user is admin or owner of set
        verifySetAccess(existingSet, EDIT_FLASHCARD_SET_ACCESS_DENIED_MESSAGE);

        existingSet.setSetName(updateDTO.setName());
        existingSet.setDescription(updateDTO.description());

        flashcardSetRepository.save(existingSet);
        return flashcardSetDTOMapper.apply(existingSet);
    }

    public void deleteFlashcardSetById(Long setId) {

        FlashcardSet existingSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(buildFlashcardSetNotFoundMessage(setId)));

        verifySetAccess(existingSet, DELETE_FLASHCARD_SET_ACCESS_DENIED_MESSAGE);

        flashcardSetRepository.delete(existingSet);
    }

    /**
     * Helper method to verify if the authenticated user has rights to modify/delete the set.
     */
    private void verifySetAccess(FlashcardSet flashcardSet, String errorMessage) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();
        ownerOrAdminPolicy.verify(
                authenticatedUser,
                flashcardSet.getUser().getUserId(),
                errorMessage);
    }

    private String buildUserNotFoundMessage(Long userId) {
        return USER_WITH_ID_MESSAGE_PREFIX + userId + NOT_FOUND_MESSAGE_SUFFIX;
    }

    private String buildUserNotFoundMessage(String nickname) {
        return USER_WITH_NICKNAME_MESSAGE_PREFIX
                + nickname
                + NOT_FOUND_MESSAGE_SUFFIX;
    }

    private String buildFlashcardSetNotFoundMessage(Long setId) {
        return FLASHCARD_SET_WITH_ID_MESSAGE_PREFIX + setId + NOT_FOUND_MESSAGE_SUFFIX;
    }
}