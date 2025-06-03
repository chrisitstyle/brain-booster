package com.brainbooster.flashcardset.dto;

public record FlashcardSetCreationDTO(
        long userId,
        String setName,
        String description){

}
