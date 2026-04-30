package com.brainbooster.flashcardset;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.mapper.FlashcardDTOMapper;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetUpdateDTO;
import com.brainbooster.flashcardset.mapper.FlashcardSetDTOMapper;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import com.brainbooster.utils.TestEntities;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardSetServiceTest {

    @Mock
    private FlashcardSetRepository flashcardSetRepository;
    @Mock
    private FlashcardRepository flashcardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FlashcardSetDTOMapper flashcardSetDTOMapper;
    @Mock
    private FlashcardDTOMapper flashcardDTOMapper;
    @InjectMocks
    private FlashcardSetService flashcardSetService;

    private FlashcardSet flashcardSet;
    private FlashcardSetDTO flashcardSetDTO;

    @BeforeEach
    void setUp() {
        flashcardSet = TestEntities.createFlashcardSet();
        flashcardSetDTO = TestEntities.createFlashcardSetDTO();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext(User user) {
        // add lenient to avoid UnnecessaryStubbingException
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(auth.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void addFlashcardSetCreationDTO_ReturnsFlashcardSetDTO() {
        // given
        FlashcardSetCreationDTO inputDTO = TestEntities.createFlashcardSetCreationDTO();
        User mockUser = TestEntities.createUser();

        when(userRepository.findById(inputDTO.userId())).thenReturn(Optional.of(mockUser));
        when(flashcardSetRepository.save(any(FlashcardSet.class))).thenReturn(flashcardSet);
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);

        // when
        FlashcardSetDTO resultDTO = flashcardSetService.addFlashcardSet(inputDTO);

        // then
        Assertions.assertThat(resultDTO).isNotNull();
        Assertions.assertThat(resultDTO.setId()).isEqualTo(flashcardSetDTO.setId());
        Assertions.assertThat(resultDTO.setName()).isEqualTo(flashcardSetDTO.setName());
        Assertions.assertThat(resultDTO.description()).isEqualTo(flashcardSetDTO.description());

        verify(userRepository, times(1)).findById(inputDTO.userId());
        verify(flashcardSetRepository, times(1)).save(any(FlashcardSet.class));
        verify(flashcardSetDTOMapper, times(1)).apply(any(FlashcardSet.class));
    }

    @Test
    void addFlashcardSet_ThrowsNoSuchElementException_WhenUserNotFound() {
        // given
        FlashcardSetCreationDTO inputDTO = TestEntities.createFlashcardSetCreationDTO();

        when(userRepository.findById(inputDTO.userId())).thenReturn(Optional.empty());

        // when, then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> flashcardSetService.addFlashcardSet(inputDTO));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("User with id: " + inputDTO.userId() + " not found");

        verify(flashcardSetRepository, never()).save(any(FlashcardSet.class));
    }

    @Test
    void getAllFlashcardSets_ReturnsAllFlashcardSetsDTO() {

        when(flashcardSetRepository.findAllWithUsers())
                .thenReturn(Collections.singletonList(flashcardSet));

        when(flashcardSetDTOMapper.apply(flashcardSet))
                .thenReturn(flashcardSetDTO);

        List<FlashcardSetDTO> result = flashcardSetService.getAllFlashcardSets();

        Assertions.assertThat(result)
                .hasSize(1)
                .containsExactly(flashcardSetDTO);
        verify(flashcardSetRepository, times(1))
                .findAllWithUsers();
    }

    @Test
    void getFlashcardSetById_ReturnsFlashcardSetDTO_WhenFlashcardSetExists() {

        when(flashcardSetRepository.findByIdWithUser(1L)).thenReturn(Optional.of(flashcardSet));
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);

        FlashcardSetDTO flashcardSetExists = flashcardSetService.getFlashcardSetById(1L);

        Assertions.assertThat(flashcardSetExists)
                .isNotNull()
                .isEqualTo(flashcardSetDTO);
    }

    @Test
    void getFlashcardSetById_ThrowsNoSuchElement_WhenFlashcardSetDoesNotExist() {
        when(flashcardSetRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> flashcardSetService.getFlashcardSetById(1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");
    }

    @Test
    void getAllFlashcardsInSet_ReturnFlashcardsDTOs_WhenFlashcardSetExists() {
        // given
        List<Flashcard> mockFlashcards = List.of(
                new Flashcard(1L, flashcardSet, "Question 1", "Answer 1"),
                new Flashcard(2L, flashcardSet, "Question 2", "Answer 2")
        );
        FlashcardDTO dto1 = new FlashcardDTO(1L, 1L, "Question 1", "Answer 1");
        FlashcardDTO dto2 = new FlashcardDTO(2L, 1L, "Question 2", "Answer 2");

        when(flashcardSetRepository.existsById(1L)).thenReturn(true);
        when(flashcardRepository.findAllByFlashcardSet_SetId(1L)).thenReturn(mockFlashcards);

        when(flashcardDTOMapper.apply(mockFlashcards.get(0))).thenReturn(dto1);
        when(flashcardDTOMapper.apply(mockFlashcards.get(1))).thenReturn(dto2);

        // when
        List<FlashcardDTO> result = flashcardSetService.getAllFlashcardsInSet(1L);

        // then
        Assertions.assertThat(result)
                .hasSize(2)
                .extracting(FlashcardDTO::term)
                .containsExactly("Question 1", "Question 2");
    }

    @Test
    void getAllFlashcardsInSet_ThrowsNoSuchElement_WhenFlashcardSetNotExists() {

        when(flashcardSetRepository.existsById(1L)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> flashcardSetService.getAllFlashcardsInSet(1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");
    }


    @Test
    void updateFlashcardSet_ReturnsUpdatedFlashcardSetDTO() {
        // given
        setupSecurityContext(TestEntities.createUser());
        FlashcardSetUpdateDTO updateDTO = TestEntities.createFlashcardSetUpdateDTO();
        when(flashcardSetRepository.findById(1L)).thenReturn(Optional.of(flashcardSet));
        when(flashcardSetRepository.save(flashcardSet)).thenReturn(flashcardSet);
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);

        // when
        FlashcardSetDTO result = flashcardSetService.updateFlashcardSet(updateDTO, 1L);

        //then
        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(flashcardSetDTO);
        verify(flashcardSetRepository, times(1)).save(flashcardSet);
    }

    @Test
    void updateFlashcardSet_ThrowsNoSuchElement_WhenFlashcardSetDoesNotExist() {
        FlashcardSetUpdateDTO updateDTO = TestEntities.createFlashcardSetUpdateDTO();

        when(flashcardSetRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> flashcardSetService.updateFlashcardSet(updateDTO, 1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");
    }


    @Test
    void deleteFlashcardSetById_ShouldDeleteFlashcardSet_WhenFlashcardSetExists() {
        setupSecurityContext(TestEntities.createUser());
        when(flashcardSetRepository.findById(1L)).thenReturn(Optional.of(flashcardSet));

        flashcardSetService.deleteFlashcardSetById(1L);

        verify(flashcardSetRepository, times(1)).delete(flashcardSet);
    }

    @Test
    void deleteFlashcardSetById_ThrowsNoSuchElement_WhenFlashcardSetDoesNotExist() {
        when(flashcardSetRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> flashcardSetService.deleteFlashcardSetById(1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");

        verify(flashcardSetRepository, never()).delete(any(FlashcardSet.class));
    }


}
