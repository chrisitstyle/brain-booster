package com.brainbooster.flashcardset.dto;

public record FlashcardSetCreationDTO(
        Long flashcardSetId,
        long userId,
        String setName,
        String description){

}
