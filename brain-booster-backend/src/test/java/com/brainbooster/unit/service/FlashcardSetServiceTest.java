package com.brainbooster.unit.service;

import com.brainbooster.model.FlashcardSet;
import com.brainbooster.model.User;
import com.brainbooster.repository.FlashcardSetRepository;
import com.brainbooster.service.FlashcardSetService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardSetServiceTest {

    @Mock
    private FlashcardSetRepository flashcardSetRepository;
    @InjectMocks
    private FlashcardSetService flashcardSetService;

    private FlashcardSet flashcardSet;

    @BeforeEach
    public void setUp() {
        flashcardSet = new FlashcardSet();
        User user = new User();
        user.setUserId(1L);
        flashcardSet.setSetId(1L);
        flashcardSet.setUser(user);
        flashcardSet.setSetName("example flashcardSet");
        flashcardSet.setDescription("example description");

    }

    @Test
    void FlashcardSetService_AddFlashcardSet_ReturnsSavedFlashcardSet() {
        when(flashcardSetRepository.save(Mockito.any(FlashcardSet.class))).thenReturn(flashcardSet);

        FlashcardSet savedFlashcardSet = flashcardSetService.addFlashcardSet(flashcardSet);

        Assertions.assertThat(savedFlashcardSet).isNotNull();
    }

    @Test
    void FlashcardSetService_GetAllFlashcardSets_ReturnsAllFlashcardSets() {

        when(flashcardSetRepository.findAll()).thenReturn(Collections.singletonList(flashcardSet));

        List<FlashcardSet> flashcardSetReturned = flashcardSetService.getAllFlashcardSets();

        assertEquals(1, flashcardSetReturned.size(), "Result size should match the size of flashcardSet");
        verify(flashcardSetRepository, times(1)).findAll();
    }

    @Test
    void FlashcardSetService_GetFlashcardSetById_ReturnsFlashcardSet_WhenFlashcardSetExists() {
        long flashcardSetId = 1L;

        when(flashcardSetRepository.findById(flashcardSetId)).thenReturn(Optional.ofNullable(flashcardSet));

        FlashcardSet flashcardSetExists = flashcardSetService.getFlashcardSetById(flashcardSetId);

        Assertions.assertThat(flashcardSetExists)
                .isNotNull()
                .isEqualTo(flashcardSet);
    }

    @Test
    void FlashcardSetService_GetFlashcardSetById_ThrowsResponseStatusException_WhenFlashcardSetDoesNotExist() {

        when(flashcardSetRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> flashcardSetService.getFlashcardSetById(1L));

        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }


}
