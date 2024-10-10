package com.brainbooster.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_set")
public class FlashcardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long setId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "set_name")
    private String setName;
    private String description;
    @Column(name = "created_at")
    private LocalDateTime createdAt;



}