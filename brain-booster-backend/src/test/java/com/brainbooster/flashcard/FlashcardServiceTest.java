package com.brainbooster.flashcard;

import com.brainbooster.flashcard.dto.FlashcardCreationDTO;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.dto.FlashcardUpdateDTO;
import com.brainbooster.flashcard.mapper.FlashcardDTOMapper;
import com.brainbooster.flashcard.starred.UserStarredFlashcard;
import com.brainbooster.flashcard.starred.UserStarredFlashcardId;
import com.brainbooster.flashcard.starred.UserStarredFlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.brainbooster.utils.TestEntities.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    private final FlashcardRepository flashcardRepository = mock(FlashcardRepository.class);
    private final FlashcardSetRepository flashcardSetRepository = mock(FlashcardSetRepository.class);
    private final UserStarredFlashcardRepository starredFlashcardRepository =
            mock(UserStarredFlashcardRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final FlashcardDTOMapper flashcardDTOMapper = mock(FlashcardDTOMapper.class);

    private final FlashcardService flashcardService = new FlashcardService(
            flashcardRepository,
            flashcardSetRepository,
            starredFlashcardRepository,
            userRepository,
            flashcardDTOMapper
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addFlashcard_ShouldCreateFlashcard_WhenUserIsSetOwner() {
        // given
        User authUser = setAuthenticatedUser(createUser());

        FlashcardSet flashcardSet = flashcardSetBuilder()
                .user(authUser)
                .build();

        FlashcardCreationDTO creationDTO = createFlashcardCreationDTO();

        Flashcard savedFlashcard = flashcardBuilder()
                .flashcardSet(flashcardSet)
                .term(creationDTO.term())
                .definition(creationDTO.definition())
                .build();

        FlashcardDTO expectedDTO = createFlashcardDTO();

        when(flashcardSetRepository.findById(creationDTO.setId()))
                .thenReturn(Optional.of(flashcardSet));
        when(flashcardRepository.save(any(Flashcard.class))).thenReturn(savedFlashcard);
        when(flashcardDTOMapper.apply(savedFlashcard)).thenReturn(expectedDTO);

        // when
        FlashcardDTO result = flashcardService.addFlashcard(creationDTO);

        // then
        assertThat(result).isEqualTo(expectedDTO);

        ArgumentCaptor<Flashcard> flashcardCaptor =
                ArgumentCaptor.forClass(Flashcard.class);

        verify(flashcardRepository).save(flashcardCaptor.capture());

        Flashcard flashcardToSave = flashcardCaptor.getValue();

        assertThat(flashcardToSave.getFlashcardSet()).isEqualTo(flashcardSet);
        assertThat(flashcardToSave.getTerm()).isEqualTo("test_term");
        assertThat(flashcardToSave.getDefinition()).isEqualTo("test_definition");
    }

    @Test
    void addFlashcard_ShouldCreateFlashcard_WhenUserIsAdmin() {
        // given
        User setOwner = createUser();
        User admin = setAuthenticatedUser(createAdminUser());

        FlashcardSet flashcardSet = flashcardSetBuilder()
                .user(setOwner)
                .build();

        FlashcardCreationDTO creationDTO = createFlashcardCreationDTO();

        Flashcard savedFlashcard = flashcardBuilder()
                .flashcardSet(flashcardSet)
                .term(creationDTO.term())
                .definition(creationDTO.definition())
                .build();

        FlashcardDTO expectedDTO = createFlashcardDTO();

        when(flashcardSetRepository.findById(creationDTO.setId()))
                .thenReturn(Optional.of(flashcardSet));
        when(flashcardRepository.save(any(Flashcard.class))).thenReturn(savedFlashcard);
        when(flashcardDTOMapper.apply(savedFlashcard)).thenReturn(expectedDTO);

        // when
        FlashcardDTO result = flashcardService.addFlashcard(creationDTO);

        // then
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(result).isEqualTo(expectedDTO);

        verify(flashcardRepository).save(any(Flashcard.class));
    }

    @Test
    void addFlashcard_ShouldThrowNoSuchElementException_WhenFlashcardSetDoesNotExist() {
        // given
        setAuthenticatedUser(createUser());

        FlashcardCreationDTO creationDTO = createFlashcardCreationDTO();

        when(flashcardSetRepository.findById(creationDTO.setId()))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> flashcardService.addFlashcard(creationDTO))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("FlashcardSet with id 1 not found");

        verify(flashcardRepository, never()).save(any());
    }

    @Test
    void addFlashcard_ShouldThrowAccessDeniedException_WhenUserIsNotSetOwner() {
        // given
        User setOwner = createUser(1L, Role.USER);
        setAuthenticatedUser(createUser(2L, Role.USER));

        FlashcardSet flashcardSet = flashcardSetBuilder()
                .user(setOwner)
                .build();

        FlashcardCreationDTO creationDTO = createFlashcardCreationDTO();

        when(flashcardSetRepository.findById(creationDTO.setId()))
                .thenReturn(Optional.of(flashcardSet));

        // when + then
        assertThatThrownBy(() -> flashcardService.addFlashcard(creationDTO))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only add flashcards to your own sets!");

        verify(flashcardRepository, never()).save(any());
    }

    @Test
    void getAllFlashcards_ShouldReturnAllFlashcards() {
        // given
        Flashcard flashcard1 = createFlashcard();

        Flashcard flashcard2 = flashcardBuilder()
                .flashcardId(2L)
                .term("test_term_2")
                .definition("test_definition_2")
                .build();

        FlashcardDTO flashcardDTO1 = createFlashcardDTO();

        FlashcardDTO flashcardDTO2 = new FlashcardDTO(
                2L,
                1L,
                "test_term_2",
                "test_definition_2",
                false
        );

        when(flashcardRepository.findAll()).thenReturn(List.of(flashcard1, flashcard2));
        when(flashcardDTOMapper.apply(flashcard1)).thenReturn(flashcardDTO1);
        when(flashcardDTOMapper.apply(flashcard2)).thenReturn(flashcardDTO2);

        // when
        List<FlashcardDTO> result = flashcardService.getAllFlashcards();

        // then
        assertThat(result).containsExactly(flashcardDTO1, flashcardDTO2);
    }

    @Test
    void getFlashcardById_ShouldReturnFlashcard_WhenFlashcardExists() {
        // given
        Flashcard flashcard = createFlashcard();
        FlashcardDTO expectedDTO = createFlashcardDTO();

        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(flashcardDTOMapper.apply(flashcard)).thenReturn(expectedDTO);

        // when
        FlashcardDTO result = flashcardService.getFlashcardById(1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);
    }

    @Test
    void getFlashcardById_ShouldThrowNoSuchElementException_WhenFlashcardDoesNotExist() {
        // given
        when(flashcardRepository.findById(999L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> flashcardService.getFlashcardById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Flashcard with id 999 not found");
    }

    @Test
    void updateFlashcard_ShouldUpdateFlashcard_WhenUserIsSetOwner() {
        // given
        User authUser = setAuthenticatedUser(createUser());

        FlashcardSet flashcardSet = flashcardSetBuilder()
                .user(authUser)
                .build();

        Flashcard existingFlashcard = flashcardBuilder()
                .flashcardSet(flashcardSet)
                .build();

        FlashcardUpdateDTO updateDTO = createFlashcardUpdateDTO();
        FlashcardDTO expectedDTO = createUpdatedFlashcardDTO();

        when(flashcardRepository.findByIdWithSetAndUser(1L))
                .thenReturn(Optional.of(existingFlashcard));
        when(flashcardRepository.save(existingFlashcard)).thenReturn(existingFlashcard);
        when(flashcardDTOMapper.apply(existingFlashcard)).thenReturn(expectedDTO);

        // when
        FlashcardDTO result = flashcardService.updateFlashcard(updateDTO, 1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);
        assertThat(existingFlashcard.getTerm()).isEqualTo("updated_term");
        assertThat(existingFlashcard.getDefinition()).isEqualTo("updated_definition");

        verify(flashcardRepository).save(existingFlashcard);
    }

    @Test
    void updateFlashcard_ShouldUpdateFlashcard_WhenUserIsAdmin() {
        // given
        User setOwner = createUser();
        setAuthenticatedUser(createAdminUser());

        FlashcardSet flashcardSet = flashcardSetBuilder()
                .user(setOwner)
                .build();

        Flashcard existingFlashcard = flashcardBuilder()
                .flashcardSet(flashcardSet)
                .build();

        FlashcardUpdateDTO updateDTO = createFlashcardUpdateDTO();
        FlashcardDTO expectedDTO = createUpdatedFlashcardDTO();

        when(flashcardRepository.findByIdWithSetAndUser(1L))
                .thenReturn(Optional.of(existingFlashcard));
        when(flashcardRepository.save(existingFlashcard)).thenReturn(existingFlashcard);
        when(flashcardDTOMapper.apply(existingFlashcard)).thenReturn(expectedDTO);

        // when
        FlashcardDTO result = flashcardService.updateFlashcard(updateDTO, 1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);
        assertThat(existingFlashcard.getTerm()).isEqualTo("updated_term");
        assertThat(existingFlashcard.getDefinition()).isEqualTo("updated_definition");

        verify(flashcardRepository).save(existingFlashcard);
    }

    @Test
    void updateFlashcard_ShouldThrowNoSuchElementException_WhenFlashcardDoesNotExist() {
        // given
        setAuthenticatedUser(createUser());

        FlashcardUpdateDTO updateDTO = createFlashcardUpdateDTO();

        when(flashcardRepository.findByIdWithSetAndUser(999L))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> flashcardService.updateFlashcard(updateDTO, 999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Flashcard with id 999 not found");

        verify(flashcardRepository, never()).save(any());
    }

    @Test
    void updateFlashcard_ShouldThrowAccessDeniedException_WhenUserIsNotSetOwner() {
        // given
        User setOwner = createUser(1L, Role.USER);
        setAuthenticatedUser(createUser(2L, Role.USER));

        FlashcardSet flashcardSet = flashcardSetBuilder()
                .user(setOwner)
                .build();

        Flashcard existingFlashcard = flashcardBuilder()
                .flashcardSet(flashcardSet)
                .build();

        FlashcardUpdateDTO updateDTO = createFlashcardUpdateDTO();

        when(flashcardRepository.findByIdWithSetAndUser(1L))
                .thenReturn(Optional.of(existingFlashcard));

        // when + then
        assertThatThrownBy(() -> flashcardService.updateFlashcard(updateDTO, 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not allowed to edit this flashcard!");

        verify(flashcardRepository, never()).save(any());
    }

    @Test
    void starFlashcard_ShouldCreateStarredRelationAndReturnStarredFlashcard() {
        // given
        User authUser = setAuthenticatedUser(createUser());
        Flashcard flashcard = createFlashcard();
        FlashcardDTO expectedDTO = createFlashcardDTO(true);

        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(starredFlashcardRepository.existsByUser_UserIdAndFlashcard_FlashcardId(1L, 1L))
                .thenReturn(false);
        when(userRepository.getReferenceById(1L)).thenReturn(authUser);
        when(flashcardDTOMapper.toDto(flashcard, true)).thenReturn(expectedDTO);

        // when
        FlashcardDTO result = flashcardService.starFlashcard(1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);
        assertThat(result.starred()).isTrue();

        ArgumentCaptor<UserStarredFlashcard> starredCaptor =
                ArgumentCaptor.forClass(UserStarredFlashcard.class);

        verify(starredFlashcardRepository).save(starredCaptor.capture());

        UserStarredFlashcard savedStarredFlashcard = starredCaptor.getValue();

        assertThat(savedStarredFlashcard.getId()).isEqualTo(
                new UserStarredFlashcardId(1L, 1L)
        );
        assertThat(savedStarredFlashcard.getUser()).isEqualTo(authUser);
        assertThat(savedStarredFlashcard.getFlashcard()).isEqualTo(flashcard);
    }

    @Test
    void starFlashcard_ShouldNotCreateDuplicateRelation_WhenFlashcardIsAlreadyStarred() {
        // given
        setAuthenticatedUser(createUser());

        Flashcard flashcard = createFlashcard();
        FlashcardDTO expectedDTO = createFlashcardDTO(true);

        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(starredFlashcardRepository.existsByUser_UserIdAndFlashcard_FlashcardId(1L, 1L))
                .thenReturn(true);
        when(flashcardDTOMapper.toDto(flashcard, true)).thenReturn(expectedDTO);

        // when
        FlashcardDTO result = flashcardService.starFlashcard(1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);
        assertThat(result.starred()).isTrue();

        verify(starredFlashcardRepository, never()).save(any());
        verify(userRepository, never()).getReferenceById(anyLong());
    }

    @Test
    void starFlashcard_ShouldThrowNoSuchElementException_WhenFlashcardDoesNotExist() {
        // given
        setAuthenticatedUser(createUser());

        when(flashcardRepository.findById(999L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> flashcardService.starFlashcard(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Flashcard with id 999 not found");

        verify(starredFlashcardRepository, never())
                .existsByUser_UserIdAndFlashcard_FlashcardId(anyLong(), anyLong());
        verify(starredFlashcardRepository, never()).save(any());
    }

    @Test
    void unstarFlashcard_ShouldDeleteStarredRelationAndReturnUnstarredFlashcard() {
        // given
        setAuthenticatedUser(createUser());

        Flashcard flashcard = createFlashcard();
        FlashcardDTO expectedDTO = createFlashcardDTO(false);

        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(flashcardDTOMapper.toDto(flashcard, false)).thenReturn(expectedDTO);

        // when
        FlashcardDTO result = flashcardService.unstarFlashcard(1L);

        // then
        assertThat(result).isEqualTo(expectedDTO);
        assertThat(result.starred()).isFalse();

        verify(starredFlashcardRepository)
                .deleteByUser_UserIdAndFlashcard_FlashcardId(1L, 1L);
    }

    @Test
    void unstarFlashcard_ShouldThrowNoSuchElementException_WhenFlashcardDoesNotExist() {
        // given
        setAuthenticatedUser(createUser());

        when(flashcardRepository.findById(999L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> flashcardService.unstarFlashcard(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Flashcard with id 999 not found");

        verify(starredFlashcardRepository, never())
                .deleteByUser_UserIdAndFlashcard_FlashcardId(anyLong(), anyLong());
    }

    @Test
    void deleteFlashcardById_ShouldDeleteFlashcard_WhenUserIsSetOwner() {
        // given
        User authUser = setAuthenticatedUser(createUser());

        FlashcardSet flashcardSet = flashcardSetBuilder()
                .user(authUser)
                .build();

        Flashcard flashcard = flashcardBuilder()
                .flashcardSet(flashcardSet)
                .build();

        when(flashcardRepository.findByIdWithSetAndUser(1L))
                .thenReturn(Optional.of(flashcard));

        // when
        flashcardService.deleteFlashcardById(1L);

        // then
        verify(flashcardRepository).delete(flashcard);
    }

    @Test
    void deleteFlashcardById_ShouldDeleteFlashcard_WhenUserIsAdmin() {
        // given
        User setOwner = createUser();
        setAuthenticatedUser(createAdminUser());

        FlashcardSet flashcardSet = flashcardSetBuilder()
                .user(setOwner)
                .build();

        Flashcard flashcard = flashcardBuilder()
                .flashcardSet(flashcardSet)
                .build();

        when(flashcardRepository.findByIdWithSetAndUser(1L))
                .thenReturn(Optional.of(flashcard));

        // when
        flashcardService.deleteFlashcardById(1L);

        // then
        verify(flashcardRepository).delete(flashcard);
    }

    @Test
    void deleteFlashcardById_ShouldThrowNoSuchElementException_WhenFlashcardDoesNotExist() {
        // given
        setAuthenticatedUser(createUser());

        when(flashcardRepository.findByIdWithSetAndUser(999L))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> flashcardService.deleteFlashcardById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Flashcard with id 999 not found");

        verify(flashcardRepository, never()).delete(any());
    }

    @Test
    void deleteFlashcardById_ShouldThrowAccessDeniedException_WhenUserIsNotSetOwner() {
        // given
        User setOwner = createUser(1L, Role.USER);
        setAuthenticatedUser(createUser(2L, Role.USER));

        FlashcardSet flashcardSet = flashcardSetBuilder()
                .user(setOwner)
                .build();

        Flashcard flashcard = flashcardBuilder()
                .flashcardSet(flashcardSet)
                .build();

        when(flashcardRepository.findByIdWithSetAndUser(1L))
                .thenReturn(Optional.of(flashcard));

        // when + then
        assertThatThrownBy(() -> flashcardService.deleteFlashcardById(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not allowed to delete this flashcard!");

        verify(flashcardRepository, never()).delete(any());
    }

    private User setAuthenticatedUser(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return user;
    }
}