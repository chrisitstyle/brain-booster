package com.brainbooster.flashcardset;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.mapper.FlashcardDTOMapper;
import com.brainbooster.flashcard.starred.StarredFlashcardService;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetUpdateDTO;
import com.brainbooster.flashcardset.mapper.FlashcardSetDTOMapper;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.security.authorization.OwnerOrAdminPolicy;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import com.brainbooster.utils.TestEntities;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardSetServiceTest {

    @Mock
    private FlashcardSetRepository flashcardSetRepository;
    @Mock
    private FlashcardRepository flashcardRepository;
    @Mock
    private StarredFlashcardService starredFlashcardService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FlashcardSetDTOMapper flashcardSetDTOMapper;
    @Mock
    private FlashcardDTOMapper flashcardDTOMapper;
    @Mock
    private OwnerOrAdminPolicy ownerOrAdminPolicy;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private FlashcardSetService flashcardSetService;

    private FlashcardSet flashcardSet;
    private FlashcardSetDTO flashcardSetDTO;

    @BeforeEach
    void setUp() {
        flashcardSet = TestEntities.createFlashcardSet();
        flashcardSetDTO = TestEntities.createFlashcardSetDTO();
    }

    @Test
    void addFlashcardSetCreationDTO_ReturnsFlashcardSetDTO() {
        // given
        AuthenticatedUser authenticatedUser = TestEntities.createAuthenticatedUser();
        Long userId = authenticatedUser.userId();

        FlashcardSetCreationDTO inputDTO = TestEntities.createFlashcardSetCreationDTO();

        User mockUser = TestEntities.createUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));

        when(flashcardSetRepository.save(any(FlashcardSet.class)))
                .thenReturn(flashcardSet);

        when(flashcardSetDTOMapper.apply(flashcardSet))
                .thenReturn(flashcardSetDTO);

        // when
        FlashcardSetDTO resultDTO = flashcardSetService.addFlashcardSet(inputDTO);

        // then
        Assertions.assertThat(resultDTO).isNotNull();
        Assertions.assertThat(resultDTO.setId())
                .isEqualTo(flashcardSetDTO.setId());
        Assertions.assertThat(resultDTO.setName())
                .isEqualTo(flashcardSetDTO.setName());
        Assertions.assertThat(resultDTO.description())
                .isEqualTo(flashcardSetDTO.description());

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository).findById(userId);
        verify(flashcardSetRepository).save(any(FlashcardSet.class));
        verify(flashcardRepository).saveAll(anyList());
        verify(flashcardSetDTOMapper).apply(any(FlashcardSet.class));
    }

    @Test
    void addFlashcardSet_ThrowsResourceNotFoundException_WhenUserNotFound() {
        // given
        Long userId = 999L;

        AuthenticatedUser authenticatedUser =
                new AuthenticatedUser(userId, Role.USER);

        FlashcardSetCreationDTO inputDTO =
                TestEntities.createFlashcardSetCreationDTO();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when + then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> flashcardSetService.addFlashcardSet(inputDTO));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with id: " + userId + " not found");

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository).findById(userId);

        verify(flashcardSetRepository, never())
                .save(any(FlashcardSet.class));
    }

    @Test
    void getAllFlashcardSets_ReturnsAllFlashcardSetsDTO() {
        // given
        when(flashcardSetRepository.findAllWithUsers())
                .thenReturn(Collections.singletonList(flashcardSet));

        when(flashcardSetDTOMapper.apply(flashcardSet))
                .thenReturn(flashcardSetDTO);

        // when
        List<FlashcardSetDTO> result = flashcardSetService.getAllFlashcardSets();

        // then
        Assertions.assertThat(result)
                .hasSize(1)
                .containsExactly(flashcardSetDTO);

        verify(flashcardSetRepository, times(1)).findAllWithUsers();
    }

    @Test
    void getFlashcardSetById_ReturnsFlashcardSetDTO_WhenFlashcardSetExists() {
        // given
        when(flashcardSetRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(flashcardSet));

        when(flashcardSetDTOMapper.apply(flashcardSet))
                .thenReturn(flashcardSetDTO);

        // when
        FlashcardSetDTO result = flashcardSetService.getFlashcardSetById(1L);

        // then
        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(flashcardSetDTO);
    }

    @Test
    void getFlashcardSetById_ThrowsResourceNotFound_WhenFlashcardSetDoesNotExist() {
        // given
        when(flashcardSetRepository.findByIdWithUser(1L))
                .thenReturn(Optional.empty());

        // when, then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> flashcardSetService.getFlashcardSetById(1L)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");
    }

    @Test
    void getAllFlashcardsInSet_ReturnFlashcardsDTOsWithStarredFalse_WhenUserIsNotAuthenticated() {
        // given
        List<Flashcard> mockFlashcards = List.of(
                new Flashcard(1L, flashcardSet, "Question 1", "Answer 1"),
                new Flashcard(2L, flashcardSet, "Question 2", "Answer 2")
        );

        FlashcardDTO dto1 = new FlashcardDTO(
                1L,
                1L,
                "Question 1",
                "Answer 1",
                false
        );

        FlashcardDTO dto2 = new FlashcardDTO(
                2L,
                1L,
                "Question 2",
                "Answer 2",
                false
        );

        when(starredFlashcardService
                .getStarredFlashcardIdsForCurrentUserInSet(1L))
                .thenReturn(Set.of());
        when(flashcardSetRepository.existsById(1L)).thenReturn(true);
        when(flashcardRepository.findAllByFlashcardSet_SetId(1L))
                .thenReturn(mockFlashcards);

        when(flashcardDTOMapper.toDto(mockFlashcards.get(0), false))
                .thenReturn(dto1);

        when(flashcardDTOMapper.toDto(mockFlashcards.get(1), false))
                .thenReturn(dto2);

        // when
        List<FlashcardDTO> result = flashcardSetService.getAllFlashcardsInSet(1L);

        // then
        Assertions.assertThat(result)
                .hasSize(2)
                .extracting(FlashcardDTO::term)
                .containsExactly("Question 1", "Question 2");

        Assertions.assertThat(result)
                .extracting(FlashcardDTO::starred)
                .containsExactly(false, false);

        verify(starredFlashcardService)
                .getStarredFlashcardIdsForCurrentUserInSet(1L);
    }

    @Test
    void getAllFlashcardsInSet_ReturnFlashcardsDTOsWithUserStarredStatus_WhenUserIsAuthenticated() {
        // given
        when(starredFlashcardService
                .getStarredFlashcardIdsForCurrentUserInSet(1L))
                .thenReturn(Set.of(2L));

        List<Flashcard> mockFlashcards = List.of(
                new Flashcard(1L, flashcardSet, "Question 1", "Answer 1"),
                new Flashcard(2L, flashcardSet, "Question 2", "Answer 2")
        );

        FlashcardDTO dto1 = new FlashcardDTO(
                1L,
                1L,
                "Question 1",
                "Answer 1",
                false);

        FlashcardDTO dto2 = new FlashcardDTO(
                2L,
                1L,
                "Question 2",
                "Answer 2",
                true);

        when(flashcardSetRepository.existsById(1L)).thenReturn(true);

        when(starredFlashcardService
                .getStarredFlashcardIdsForCurrentUserInSet(1L))
                .thenReturn(Set.of(2L));

        when(flashcardRepository.findAllByFlashcardSet_SetId(1L))
                .thenReturn(mockFlashcards);

        when(flashcardDTOMapper.toDto(mockFlashcards.get(0), false))
                .thenReturn(dto1);

        when(flashcardDTOMapper.toDto(mockFlashcards.get(1), true))
                .thenReturn(dto2);

        // when
        List<FlashcardDTO> result = flashcardSetService.getAllFlashcardsInSet(1L);

        // then
        Assertions.assertThat(result)
                .hasSize(2)
                .extracting(FlashcardDTO::term)
                .containsExactly("Question 1", "Question 2");

        Assertions.assertThat(result)
                .extracting(FlashcardDTO::starred)
                .containsExactly(false, true);

        verify(starredFlashcardService)
                .getStarredFlashcardIdsForCurrentUserInSet(1L);
    }

    @Test
    void getAllFlashcardsInSet_ThrowsResourceNotFound_WhenFlashcardSetNotExists() {
        // given
        when(flashcardSetRepository.existsById(1L)).thenReturn(false);

        // when + then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> flashcardSetService.getAllFlashcardsInSet(1L)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");

        verify(flashcardRepository, never()).findAllByFlashcardSet_SetId(anyLong());
        verifyNoInteractions(starredFlashcardService);
    }

    @Test
    void getAllFlashcardSetsByUserId_ShouldReturnFlashcardSets_WhenUserExists() {
        // given
        Long userId = 1L;

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(flashcardSetRepository.findByUserId(userId))
                .thenReturn(List.of(flashcardSet));

        when(flashcardSetDTOMapper.apply(flashcardSet))
                .thenReturn(flashcardSetDTO);

        // when
        List<FlashcardSetDTO> result =
                flashcardSetService.getAllFlashcardSetsByUserId(userId);

        // then
        Assertions.assertThat(result)
                .containsExactly(flashcardSetDTO);

        verify(userRepository).existsById(userId);
        verify(flashcardSetRepository).findByUserId(userId);
        verify(flashcardSetDTOMapper).apply(flashcardSet);
    }

    @Test
    void getAllFlashcardSetsByUserId_ShouldThrowResourceNotFound_WhenUserDoesNotExist() {
        // given
        Long userId = 1L;

        when(userRepository.existsById(userId))
                .thenReturn(false);

        // when, then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> flashcardSetService.getAllFlashcardSetsByUserId(userId));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with id: 1 not found");

        verify(flashcardSetRepository, never())
                .findByUserId(anyLong());
    }

    @Test
    void getAllFlashcardSetsByUserNickname_ShouldReturnFlashcardSets_WhenUserExists() {
        // given
        String nickname = "johndoe";

        when(userRepository.existsByNickname(nickname))
                .thenReturn(true);

        when(flashcardSetRepository.findAllByUserNickname(nickname))
                .thenReturn(List.of(flashcardSet));

        when(flashcardSetDTOMapper.apply(flashcardSet))
                .thenReturn(flashcardSetDTO);

        // when
        List<FlashcardSetDTO> result = flashcardSetService.getAllFlashcardSetsByUserNickname(nickname);

        // then
        Assertions.assertThat(result)
                .containsExactly(flashcardSetDTO);

        verify(userRepository).existsByNickname(nickname);
        verify(flashcardSetRepository)
                .findAllByUserNickname(nickname);
    }

    @Test
    void getAllFlashcardSetsByUserNickname_ShouldThrowResourceNotFound_WhenUserDoesNotExist() {
        // given
        String nickname = "unknown";

        when(userRepository.existsByNickname(nickname))
                .thenReturn(false);

        // when + then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> flashcardSetService
                        .getAllFlashcardSetsByUserNickname(nickname)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with nickname: unknown not found");

        verify(flashcardSetRepository, never())
                .findAllByUserNickname(anyString());
    }

    @Test
    void updateFlashcardSet_ReturnsUpdatedFlashcardSetDTO() {
        // given
        AuthenticatedUser authUser = TestEntities.createAuthenticatedUser();
        when(currentUserProvider.getCurrentUser()).thenReturn(authUser);

        FlashcardSetUpdateDTO updateDTO = TestEntities.createFlashcardSetUpdateDTO();

        when(flashcardSetRepository.findById(1L))
                .thenReturn(Optional.of(flashcardSet));

        when(flashcardSetRepository.save(flashcardSet))
                .thenReturn(flashcardSet);

        when(flashcardSetDTOMapper.apply(flashcardSet))
                .thenReturn(flashcardSetDTO);

        // when
        FlashcardSetDTO result = flashcardSetService.updateFlashcardSet(updateDTO, 1L);

        // then
        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(flashcardSetDTO);

        verify(ownerOrAdminPolicy).verify(
                authUser,
                flashcardSet.getUser().getUserId(),
                "You are not allowed to edit this flashcard set!"
        );
        verify(flashcardSetRepository, times(1)).save(flashcardSet);
    }

    @Test
    void updateFlashcardSet_ThrowsResourceNotFound_WhenFlashcardSetDoesNotExist() {
        // given
        FlashcardSetUpdateDTO updateDTO = TestEntities.createFlashcardSetUpdateDTO();

        when(flashcardSetRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when + then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> flashcardSetService.updateFlashcardSet(updateDTO, 1L)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");
    }

    @Test
    void deleteFlashcardSetById_ShouldDeleteFlashcardSet_WhenFlashcardSetExists() {
        // given
        AuthenticatedUser authUser = TestEntities.createAuthenticatedUser();
        when(currentUserProvider.getCurrentUser()).thenReturn(authUser);

        when(flashcardSetRepository.findById(1L))
                .thenReturn(Optional.of(flashcardSet));

        // when
        flashcardSetService.deleteFlashcardSetById(1L);

        // then
        verify(ownerOrAdminPolicy).verify(
                authUser,
                flashcardSet.getUser().getUserId(),
                "You are not allowed to delete this flashcard set!");
        verify(flashcardSetRepository, times(1)).delete(flashcardSet);
    }

    @Test
    void deleteFlashcardSetById_ThrowsResourceNotFound_WhenFlashcardSetDoesNotExist() {
        // given
        when(flashcardSetRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when + then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> flashcardSetService.deleteFlashcardSetById(1L)
        );

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");

        verify(flashcardSetRepository, never()).delete(any(FlashcardSet.class));
    }
}