package com.brainbooster.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
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
