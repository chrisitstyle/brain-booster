package com.brainbooster.flashcard;

import com.brainbooster.flashcardset.FlashcardSet;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "flashcard")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flashcardId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id")
    private FlashcardSet flashcardSet;
    private String term;
    private String definition;
}
