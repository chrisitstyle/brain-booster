package com.brainbooster.spacedrepetition.state;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flashcard_review_states", uniqueConstraints = {
        @UniqueConstraint(name = "uk_flashcard_review_states_user_flashcard",
                columnNames = {"user_id", "flashcard_id"})
})
public class FlashcardReviewState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_state_id")
    private Long reviewStateId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private ReviewStage state = ReviewStage.NEW;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "stability")
    private Double stability;

    @Column(name = "difficulty")
    private Double difficulty;

    // preserve the complete library Card state for lossless round-tripping
    @Column(name = "fsrs_card_data", columnDefinition = "text")
    private String fsrsCardData;

    @Builder.Default
    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0;

    @Builder.Default
    @Column(name = "lapses", nullable = false)
    private int lapses = 0;

    @Builder.Default
    @Column(name = "algorithm_version", nullable = false, length = 20)
    private String algorithmVersion = "FSRS-6";

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
