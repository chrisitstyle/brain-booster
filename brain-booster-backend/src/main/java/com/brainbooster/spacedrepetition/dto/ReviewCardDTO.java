package com.brainbooster.spacedrepetition.dto;

import com.brainbooster.spacedrepetition.state.ReviewStage;

import java.time.Instant;

/**
 * Represents one flashcard available in the review queue.
 *
 * <p>New cards have stage NEW and no due timestamp because
 * they do not yet have a persisted FSRS state.
 *
 * @param flashcardId flashcard identifier
 * @param term front side of the flashcard
 * @param definition back side of the flashcard
 * @param stage current learning stage
 * @param dueAt scheduled due date, or null for a new card
 */
public record ReviewCardDTO(
        Long flashcardId,
        String term,
        String definition,
        ReviewStage stage,
        Instant dueAt) { }
