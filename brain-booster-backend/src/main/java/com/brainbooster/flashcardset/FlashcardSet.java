package com.brainbooster.flashcardset;

import com.brainbooster.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "flashcard_set")
public class FlashcardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long setId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "set_name")
    private String setName;
    private String description;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Formula("(SELECT COUNT(*) FROM flashcard f WHERE f.set_id = set_id)")
    private Long termCount;

}
