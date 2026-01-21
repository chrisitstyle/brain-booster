package com.brainbooster.flashcardset;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.mapper.FlashcardSetCreationDTOMapper;
import com.brainbooster.flashcardset.mapper.FlashcardSetDTOMapper;
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
    private FlashcardSetDTOMapper flashcardSetDTOMapper;
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
    void addFlashcardSetCreationDTO_ReturnsSavedFlashcardSetCreationDTO() {
        // given
        FlashcardSetCreationDTO inputDTO = TestEntities.createFlashcardSetCreationDTO();

        FlashcardSet savedFlashcardSet = FlashcardSetCreationDTOMapper.toEntity(inputDTO);
        savedFlashcardSet.setSetId(1L);

        when(flashcardSetRepository.save(any())).thenReturn(savedFlashcardSet);

        // when
        FlashcardSetCreationDTO resultDTO = flashcardSetService.addFlashcardSet(inputDTO);

        // then
        Assertions.assertThat(resultDTO).isNotNull();
        Assertions.assertThat(resultDTO.userId()).isEqualTo(1L);
        Assertions.assertThat(resultDTO.setName()).isEqualTo("test_flashcardset_name");
        Assertions.assertThat(resultDTO.description()).isEqualTo("test_flashcardset_description");


        verify(flashcardSetRepository, times(1)).save(any(FlashcardSet.class));

    }

    @Test
    void addFlashcardSet_ShouldUseMappersCorrectly() {
        // given
        FlashcardSetCreationDTO inputDTO = TestEntities.createFlashcardSetCreationDTO();
        FlashcardSet savedEntity = TestEntities.createFlashcardSet();

        when(flashcardSetRepository.save(any(FlashcardSet.class))).thenReturn(savedEntity);

        // when
        FlashcardSetCreationDTO result = flashcardSetService.addFlashcardSet(inputDTO);

        // then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.userId()).isEqualTo(1L);
        Assertions.assertThat(result.setName()).isEqualTo(inputDTO.setName());
        Assertions.assertThat(result.setName()).isEqualTo("test_flashcardset_name");

        verify(flashcardSetRepository).save(any(FlashcardSet.class));
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
    void getAllFlashcardsInSet_ReturnFlashcards_WhenFlashcardSetExists() {

        List<Flashcard> mockFlashcards = List.of(
                new Flashcard(1L, 1L, "Question 1", "Answer 1"),
                new Flashcard(2L, 1L, "Question 2", "Answer 2")
        );


        when(flashcardRepository.findAllBySetId(1L)).thenReturn(Optional.of(mockFlashcards));

        List<Flashcard> result = flashcardSetService.getAllFlashcardsInSet(1L);

        Assertions.assertThat(result)
                .hasSize(2)
                .extracting(Flashcard::getTerm)
                .containsExactly("Question 1", "Question 2");
    }

    @Test
    void getAllFlashcardsInSet_ThrowsNoSuchElement_WhenFlashcardSetNotExists() {

        when(flashcardRepository.findAllBySetId(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> flashcardSetService.getAllFlashcardsInSet(1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");
    }


    @Test
    void updateFlashcardSet_ReturnsUpdatedFlashcardSetDTO() {

        when(flashcardSetRepository.findById(1L)).thenReturn(Optional.of(flashcardSet));
        when(flashcardSetRepository.save(flashcardSet)).thenReturn(flashcardSet);
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);

        FlashcardSetDTO result = flashcardSetService.updateFlashcardSet(flashcardSet, 1L);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(flashcardSetDTO);
        verify(flashcardSetRepository, times(1)).save(flashcardSet);
    }

    @Test
    void updateFlashcardSet_ThrowsNoSuchElement_WhenFlashcardSetDoesNotExist() {

        when(flashcardSetRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> flashcardSetService.updateFlashcardSet(flashcardSet, 1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");
    }


    @Test
    void deleteFlashcardSetById_ShouldDeleteFlashcardSet_WhenFlashcardSetExists() {
        when(flashcardSetRepository.existsById(1L)).thenReturn(true);

        flashcardSetService.deleteFlashcardSetById(1L);

        verify(flashcardSetRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteFlashcardSetById_ThrowsNoSuchElement_WhenFlashcardSetDoesNotExist() {
        when(flashcardSetRepository.existsById(1L)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> flashcardSetService.deleteFlashcardSetById(1L));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("FlashcardSet with id: 1 not found");

        verify(flashcardSetRepository, never()).deleteById(anyLong());
    }


}
