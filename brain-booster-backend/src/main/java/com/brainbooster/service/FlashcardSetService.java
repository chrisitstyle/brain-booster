package com.brainbooster.service;

import com.brainbooster.dto.FlashcardSetDTO;
import com.brainbooster.dtomapper.FlashcardSetDTOMapper;
import com.brainbooster.model.Flashcard;
import com.brainbooster.model.FlashcardSet;
import com.brainbooster.repository.FlashcardRepository;
import com.brainbooster.repository.FlashcardSetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashcardSetService {
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetDTOMapper flashcardSetDTOMapper;

    public FlashcardSetDTO addFlashcardSet(FlashcardSet flashcardSet) {

        if(flashcardSet.getCreatedAt() == null){
            flashcardSet.setCreatedAt(LocalDateTime.now());
        }

        FlashcardSet savedFlashcardSet = flashcardSetRepository.save(flashcardSet);
        return flashcardSetDTOMapper.apply(savedFlashcardSet);
    }

    public List<FlashcardSetDTO> getAllFlashcardSets() {
        return flashcardSetRepository.findAll(Sort.by(Sort.Direction.ASC, "setId" ))
                .stream()
                .map(flashcardSetDTOMapper)
                .toList();
    }

    public FlashcardSetDTO getFlashcardSetById(Long setId) {
        return flashcardSetRepository.findById(setId)
                .map(flashcardSetDTOMapper)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FlashcardSet with id: " + setId + " not found"));
    }

    public List<Flashcard> getAllFlashcardsInSet(Long setId){
        return flashcardRepository.findAllBySetId(setId);
    }

    @Transactional
    public FlashcardSetDTO updateFlashcardSet(FlashcardSet updatedFlashcardSet, Long setId) {

        return flashcardSetRepository.findById(setId)
                .map(flashcardSet -> {
                    flashcardSet.setSetName(updatedFlashcardSet.getSetName());
                    flashcardSet.setDescription(updatedFlashcardSet.getDescription());

                    FlashcardSet savedFlashcardSet = flashcardSetRepository.save(flashcardSet);

                    return flashcardSetDTOMapper.apply(savedFlashcardSet);
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FlashcardSet with id: " + setId + " not found"));
    }

    public void deleteFlashcardSetById(Long setId) {

        Optional<FlashcardSet> flashcardSetExists = flashcardSetRepository.findById(setId);
        if (flashcardSetExists.isPresent()) {
            flashcardSetRepository.deleteById(setId);
        } else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "FlashcardSet with id: " + setId + " not found");
        }
    }
}
