package com.brainbooster.gameresult;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "game_results", uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_game_results_user_set_mode",
                        columnNames = {"user_id", "set_id", "mode"}
                )
        }
)
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

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

    @PrePersist
    void prePersist() {
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
}
