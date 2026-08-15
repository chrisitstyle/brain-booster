package com.brainbooster.flashcard.starred;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.mapper.FlashcardDTOMapper;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class StarredFlashcardService {

    private static final String FLASHCARD_WITH_ID_MESSAGE_PREFIX = "Flashcard with id ";
    private static final String NOT_FOUND_MESSAGE_SUFFIX = " not found";

    private final FlashcardRepository flashcardRepository;
    private final UserStarredFlashcardRepository starredFlashcardRepository;
    private final UserRepository userRepository;
    private final FlashcardDTOMapper flashcardDTOMapper;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public FlashcardDTO starFlashcard(Long flashcardId) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        buildFlashcardNotFoundMessage(flashcardId)));

        boolean alreadyStarred = starredFlashcardRepository
                .existsByUser_UserIdAndFlashcard_FlashcardId(
                        authenticatedUser.userId(),
                        flashcardId
                );

        if (!alreadyStarred) {
            User userReference = userRepository.getReferenceById(authenticatedUser.userId());

            UserStarredFlashcard starredFlashcard = UserStarredFlashcard.builder()
                    .id(new UserStarredFlashcardId(
                            authenticatedUser.userId(),
                            flashcardId
                    ))
                    .user(userReference)
                    .flashcard(flashcard)
                    .build();

            starredFlashcardRepository.save(starredFlashcard);
        }

        return flashcardDTOMapper.toDto(flashcard, true);
    }

    @Transactional
    public FlashcardDTO unstarFlashcard(Long flashcardId) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        buildFlashcardNotFoundMessage(flashcardId)));

        starredFlashcardRepository
                .deleteByUser_UserIdAndFlashcard_FlashcardId(
                        authenticatedUser.userId(),
                        flashcardId);

        return flashcardDTOMapper.toDto(flashcard, false);
    }

    public Set<Long> getStarredFlashcardIdsForCurrentUserInSet(Long setId) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUserOrNull();

        if (authenticatedUser == null) {
            return Set.of();
        }

        return starredFlashcardRepository
                .findStarredFlashcardIdsByUserIdAndSetId(
                        authenticatedUser.userId(),
                        setId);
    }

    private String buildFlashcardNotFoundMessage(Long flashcardId) {
        return FLASHCARD_WITH_ID_MESSAGE_PREFIX
                + flashcardId
                + NOT_FOUND_MESSAGE_SUFFIX;
    }
}
