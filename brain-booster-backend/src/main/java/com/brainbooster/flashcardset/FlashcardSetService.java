package com.brainbooster.flashcardset;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FlashcardSetService {
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetDTOMapper flashcardSetDTOMapper;

    public FlashcardSetDTO addFlashcardSet(FlashcardSet flashcardSet) {

            flashcardSet.setCreatedAt(LocalDateTime.now());

        FlashcardSet savedFlashcardSet = flashcardSetRepository.save(flashcardSet);
        return flashcardSetDTOMapper.apply(savedFlashcardSet);
    }

    public List<FlashcardSetDTO> getAllFlashcardSets() {
        return flashcardSetRepository.findAll(Sort.by(Sort.Direction.ASC, "setId" ))
                .stream()
                .map(flashcardSetDTOMapper)
                .toList();
    }

    public FlashcardSetDTO getFlashcardSetById(long setId) {
        return flashcardSetRepository.findById(setId)
                .map(flashcardSetDTOMapper)
                .orElseThrow(() -> new NoSuchElementException("FlashcardSet with id: " + setId + " not found"));
    }

    public List<Flashcard> getAllFlashcardsInSet(long setId){

        return flashcardRepository.findAllBySetId(setId)
                .orElseThrow(() -> new NoSuchElementException("FlashcardSet with id: " + setId + " not found"));

    }

    @Transactional
    public FlashcardSetDTO updateFlashcardSet(FlashcardSet updatedFlashcardSet, long setId) {

      FlashcardSet existingSet = flashcardSetRepository.findById(setId)
              .orElseThrow(() -> new NoSuchElementException("FlashcardSet with id: " + setId + " not found"));

        existingSet.setSetName(updatedFlashcardSet.getSetName());
        existingSet.setDescription(updatedFlashcardSet.getDescription());

         flashcardSetRepository.save(existingSet);

        return flashcardSetDTOMapper.apply(existingSet);
    }

    public void deleteFlashcardSetById(long setId) {

        if (!flashcardSetRepository.existsById(setId)) {
            throw new NoSuchElementException("FlashcardSet with id: " + setId + " not found");
        }
        flashcardSetRepository.deleteById(setId);
    }
}
