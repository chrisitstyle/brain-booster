package com.brainbooster.flashcard.starred;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UserStarredFlashcardId implements Serializable {

    private Long userId;

    private Long flashcardId;
}
