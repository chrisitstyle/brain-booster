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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.Set;

import static com.brainbooster.utils.TestEntities.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class StarredFlashcardServiceTest {

    private final FlashcardRepository flashcardRepository =
            mock(FlashcardRepository.class);

    private final UserStarredFlashcardRepository starredFlashcardRepository =
            mock(UserStarredFlashcardRepository.class);

    private final UserRepository userRepository =
            mock(UserRepository.class);

    private final FlashcardDTOMapper flashcardDTOMapper =
            mock(FlashcardDTOMapper.class);

    private final CurrentUserProvider currentUserProvider =
            mock(CurrentUserProvider.class);

    private final StarredFlashcardService starredFlashcardService =
            new StarredFlashcardService(
                    flashcardRepository,
                    starredFlashcardRepository,
                    userRepository,
                    flashcardDTOMapper,
                    currentUserProvider
            );


    @Test
    void starFlashcard_ShouldCreateStarredRelationAndReturnStarredFlashcard() {
        // given
        User user = createUser();
        AuthenticatedUser authenticatedUser = createAuthenticatedUser();
        Flashcard flashcard = createFlashcard();
        FlashcardDTO expectedDTO = createFlashcardDTO(true);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(flashcardRepository.findById(1L))
                .thenReturn(Optional.of(flashcard));

        when(starredFlashcardRepository
                .existsByUser_UserIdAndFlashcard_FlashcardId(1L, 1L))
                .thenReturn(false);

        when(userRepository.getReferenceById(1L))
                .thenReturn(user);

        when(flashcardDTOMapper.toDto(flashcard, true))
                .thenReturn(expectedDTO);

        // when
        FlashcardDTO result = starredFlashcardService.starFlashcard(1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);
        assertThat(result.starred()).isTrue();

        ArgumentCaptor<UserStarredFlashcard> starredCaptor = ArgumentCaptor
                .forClass(UserStarredFlashcard.class);

        verify(starredFlashcardRepository)
                .save(starredCaptor.capture());

        UserStarredFlashcard savedStarredFlashcard = starredCaptor.getValue();

        assertThat(savedStarredFlashcard.getId())
                .isEqualTo(new UserStarredFlashcardId(1L, 1L));

        assertThat(savedStarredFlashcard.getUser())
                .isEqualTo(user);

        assertThat(savedStarredFlashcard.getFlashcard())
                .isEqualTo(flashcard);
    }

    @Test
    void starFlashcard_ShouldNotCreateDuplicateRelation_WhenFlashcardIsAlreadyStarred() {
        // given
        AuthenticatedUser authenticatedUser = createAuthenticatedUser();
        Flashcard flashcard = createFlashcard();
        FlashcardDTO expectedDTO = createFlashcardDTO(true);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(flashcardRepository.findById(1L))
                .thenReturn(Optional.of(flashcard));

        when(starredFlashcardRepository
                .existsByUser_UserIdAndFlashcard_FlashcardId(1L, 1L))
                .thenReturn(true);

        when(flashcardDTOMapper.toDto(flashcard, true))
                .thenReturn(expectedDTO);

        // when
        FlashcardDTO result = starredFlashcardService.starFlashcard(1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);
        assertThat(result.starred()).isTrue();

        verify(starredFlashcardRepository, never()).save(any());
        verify(userRepository, never()).getReferenceById(anyLong());
    }

    @Test
    void starFlashcard_ShouldThrowResourceNotFoundException_WhenFlashcardDoesNotExist() {
        // given
        AuthenticatedUser authenticatedUser = createAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(flashcardRepository.findById(999L))
                .thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> starredFlashcardService.starFlashcard(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Flashcard with id 999 not found");

        verify(starredFlashcardRepository, never())
                .existsByUser_UserIdAndFlashcard_FlashcardId(
                        anyLong(),
                        anyLong());

        verify(starredFlashcardRepository, never()).save(any());
    }

    @Test
    void unstarFlashcard_ShouldDeleteStarredRelationAndReturnUnstarredFlashcard() {
        // given
        AuthenticatedUser authenticatedUser = createAuthenticatedUser();
        Flashcard flashcard = createFlashcard();
        FlashcardDTO expectedDTO = createFlashcardDTO(false);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(flashcardRepository.findById(1L))
                .thenReturn(Optional.of(flashcard));

        when(flashcardDTOMapper.toDto(flashcard, false))
                .thenReturn(expectedDTO);

        // when
        FlashcardDTO result = starredFlashcardService.unstarFlashcard(1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);
        assertThat(result.starred()).isFalse();

        verify(starredFlashcardRepository)
                .deleteByUser_UserIdAndFlashcard_FlashcardId(1L, 1L);
    }

    @Test
    void unstarFlashcard_ShouldThrowResourceNotFoundException_WhenFlashcardDoesNotExist() {
        // given
        AuthenticatedUser authenticatedUser = createAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(flashcardRepository.findById(999L))
                .thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> starredFlashcardService.unstarFlashcard(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Flashcard with id 999 not found");

        verify(starredFlashcardRepository, never())
                .deleteByUser_UserIdAndFlashcard_FlashcardId(
                        anyLong(),
                        anyLong());
    }

    @Test
    void getStarredFlashcardIdsForCurrentUserInSet_ShouldReturnEmptySet_WhenUserIsAnonymous() {
        // given
        when(currentUserProvider.getCurrentUserOrNull())
                .thenReturn(null);

        // when
        Set<Long> result = starredFlashcardService.getStarredFlashcardIdsForCurrentUserInSet(1L);

        // then
        assertThat(result).isEmpty();

        verify(starredFlashcardRepository, never())
                .findStarredFlashcardIdsByUserIdAndSetId(
                        anyLong(),
                        anyLong());
    }

    @Test
    void getStarredFlashcardIdsForCurrentUserInSet_ShouldReturnStarredIds_WhenUserIsAuthenticated() {
        // given
        AuthenticatedUser authenticatedUser = createAuthenticatedUser();

        when(currentUserProvider.getCurrentUserOrNull())
                .thenReturn(authenticatedUser);

        when(starredFlashcardRepository
                .findStarredFlashcardIdsByUserIdAndSetId(1L, 5L))
                .thenReturn(Set.of(10L, 20L));

        // when
        Set<Long> result = starredFlashcardService
                .getStarredFlashcardIdsForCurrentUserInSet(5L);

        // then
        assertThat(result).containsExactlyInAnyOrder(10L, 20L);

        verify(starredFlashcardRepository)
                .findStarredFlashcardIdsByUserIdAndSetId(1L, 5L);
    }
}
