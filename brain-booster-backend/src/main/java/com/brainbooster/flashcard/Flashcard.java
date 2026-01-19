package com.brainbooster.flashcard;

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
    @Column(name = "set_id")
    private Long setId;
    private String term;
    private String definition;
}
