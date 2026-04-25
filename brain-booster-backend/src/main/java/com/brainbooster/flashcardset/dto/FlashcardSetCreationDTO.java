package com.brainbooster.flashcardset.dto;

public record FlashcardSetCreationDTO(
        Long flashcardSetId,
        Long userId,
        String setName,
        String description){

}
