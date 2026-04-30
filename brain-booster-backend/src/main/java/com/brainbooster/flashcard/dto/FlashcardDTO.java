package com.brainbooster.flashcard.dto;

public record FlashcardDTO(
        Long flashcardId,
        Long setId,
        String term,
        String definition
) {}
