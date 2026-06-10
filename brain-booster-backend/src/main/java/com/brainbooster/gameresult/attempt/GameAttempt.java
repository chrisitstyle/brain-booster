package com.brainbooster.gameresult.attempt;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.gameresult.GameMode;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import com.brainbooster.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "game_attempts",
        indexes = {
                @Index(
                        name = "idx_game_attempts_user_id_completed_at",
                        columnList = "user_id, completed_at"
                ),
                @Index(
                        name = "idx_game_attempts_set_id_completed_at",
                        columnList = "set_id, completed_at"
                ),
                @Index(
                        name = "idx_game_attempts_user_set_mode_completed_at",
                        columnList = "user_id, set_id, mode, completed_at"
                )
        }
)
public class GameAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private FlashcardSet set;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GameMode mode;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "attempt",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<GameQuestionResult> questionResults = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    /**
     * Adds a question result to this attempt and keeps both sides of the
     * bidirectional relationship in sync.
     *
     * @param questionResult question result that should be attached to this attempt.
     */
    public void addQuestionResult(GameQuestionResult questionResult) {
        questionResults.add(questionResult);
        questionResult.setAttempt(this);
    }
}
