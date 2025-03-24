package com.brainbooster.unit.service;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcardset.*;
import com.brainbooster.user.User;
import com.brainbooster.user.UserDTOMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardSetServiceTest {

    @Mock
    private FlashcardSetRepository flashcardSetRepository;
    @Mock
    private FlashcardRepository flashcardRepository;
    @Mock
    private FlashcardSetDTOMapper flashcardSetDTOMapper;
    @Mock
    private UserDTOMapper userDTOMapper;
    @InjectMocks
    private FlashcardSetService flashcardSetService;

    private FlashcardSet flashcardSet;
    private FlashcardSetDTO flashcardSetDTO;

    @BeforeEach
    void setUp() {
        flashcardSet = new FlashcardSet();
        User user = new User();
        user.setUserId(1L);
        flashcardSet.setSetId(1L);
        flashcardSet.setUser(user);
        flashcardSet.setSetName("example flashcardSet");
        flashcardSet.setDescription("example description");
        flashcardSet.setCreatedAt(LocalDateTime.parse("2024-11-13T00:28:05.738221"));

        flashcardSetDTO = new FlashcardSetDTO(flashcardSet.getSetId(),
                userDTOMapper.apply(user),
                flashcardSet.getSetName(),
                flashcardSet.getDescription(),
                flashcardSet.getCreatedAt());

    }

    @Test
    void addFlashcardSet_ReturnsSavedFlashcardSetDTO() {
        when(flashcardSetRepository.save(Mockito.any(FlashcardSet.class))).thenReturn(flashcardSet);
        when(flashcardSetDTOMapper.apply(Mockito.any(FlashcardSet.class))).thenReturn(flashcardSetDTO);

        FlashcardSetDTO savedFlashcardSet = flashcardSetService.addFlashcardSet(flashcardSet);

        Assertions.assertThat(savedFlashcardSet).isNotNull();
        Assertions.assertThat(savedFlashcardSet.setId()).isPositive();

    }

    @Test
    void getAllFlashcardSets_ReturnsAllFlashcardSetsDTO() {

        when(flashcardSetRepository.findAll(Sort.by(Sort.Direction.ASC, "setId")))
                .thenReturn(Collections.singletonList(flashcardSet));

        when(flashcardSetDTOMapper.apply(any(FlashcardSet.class)))
                .thenReturn(flashcardSetDTO);

        List<FlashcardSetDTO> flashcardSetsDTOReturned = flashcardSetService.getAllFlashcardSets();

        assertEquals(1, flashcardSetsDTOReturned.size(),
                "Result size should match the size of flashcardSet");
        verify(flashcardSetRepository, times(1))
                .findAll(Sort.by(Sort.Direction.ASC, "setId"));
    }

    @Test
    void getFlashcardSetById_ReturnsFlashcardSetDTO_WhenFlashcardSetExists() {
        long flashcardSetId = 1L;

        when(flashcardSetRepository.findById(flashcardSetId)).thenReturn(Optional.ofNullable(flashcardSet));
        when(flashcardSetDTOMapper.apply(Mockito.any(FlashcardSet.class))).thenReturn(flashcardSetDTO);

        FlashcardSetDTO flashcardSetExists = flashcardSetService.getFlashcardSetById(flashcardSetId);

        Assertions.assertThat(flashcardSetExists)
                .isNotNull()
                .isEqualTo(flashcardSetDTO);
    }

    @Test
    void getFlashcardSetById_ThrowsResponseStatusException_WhenFlashcardSetDoesNotExist() {

        when(flashcardSetRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> flashcardSetService.getFlashcardSetById(1L));

        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getAllFlashcardsInSet_ReturnFlashcards_WhenFlashcardSetExists() {
        Long setId = 1L;
        List<Flashcard> mockFlashcards = Arrays.asList(
                new Flashcard(1L, 1L, "Question 1", "Answer 1"),
                new Flashcard(2L, 1L, "Question 2", "Answer 2")
        );

        when(flashcardSetRepository.existsById(setId)).thenReturn(true);
        when(flashcardRepository.findAllBySetId(setId)).thenReturn(mockFlashcards);

        List<Flashcard> flashcards = flashcardSetService.getAllFlashcardsInSet(setId);

        assertEquals(2, flashcards.size());
        assertEquals("Question 1", flashcards.get(0).getTerm());
        assertEquals("Question 2", flashcards.get(1).getTerm());
    }

    @Test
    void getAllFlashcardsInSet_ThrowsResponseStatusException_WhenFlashcardSetNotExists() {
        Long setId = 1L;
        when(flashcardSetRepository.existsById(setId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> flashcardSetService.getAllFlashcardsInSet(setId));

        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }


    @Test
    void updateFlashcardSet_ReturnsUpdatedFlashcardSetDTO() {
        long setId = 1L;

        when(flashcardSetRepository.findById(setId)).thenReturn(Optional.of(flashcardSet));
        when(flashcardSetRepository.save(flashcardSet)).thenReturn(flashcardSet);
        when(flashcardSetDTOMapper.apply(flashcardSet)).thenReturn(flashcardSetDTO);

        FlashcardSetDTO updatedFlashcardSetDTO = flashcardSetService.updateFlashcardSet(flashcardSet, setId);

        Assertions.assertThat(updatedFlashcardSetDTO)
                .isNotNull()
                .isEqualTo(flashcardSetDTO);
    }

    @Test
    void updateFlashcardSet_ThrowsResponseStatusException_WhenFlashcardSetDoesNotExist() {
        long setId = 1L;

        when(flashcardSetRepository.findById(setId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> flashcardSetService.updateFlashcardSet(flashcardSet, setId));

        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }


    @Test
    void deleteFlashcardSetById_ShouldDeleteFlashcardSet_WhenFlashcardSetExists() {
        when(flashcardSetRepository.findById(1L)).thenReturn(Optional.of(flashcardSet));

        flashcardSetService.deleteFlashcardSetById(1L);

        assertAll(() -> flashcardSetService.deleteFlashcardSetById(1L));
    }

    @Test
    void deleteFlashcardSetById_ThrowsResponseStatusException_WhenFlashcardSetDoesNotExist() {
        when(flashcardSetRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> flashcardSetService.deleteFlashcardSetById(1L));

        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        verify(flashcardSetRepository, never()).deleteById(anyLong());
    }


}
