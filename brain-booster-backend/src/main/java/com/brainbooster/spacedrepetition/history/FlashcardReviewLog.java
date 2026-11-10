package com.brainbooster.spacedrepetition.history;

import com.brainbooster.spacedrepetition.state.FlashcardReviewState;
import com.brainbooster.spacedrepetition.state.ReviewStage;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flashcard_review_logs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_flashcard_review_logs_request_id",
                columnNames = "request_id")
})
public class FlashcardReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_log_id")
    private Long reviewLogId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_state_id", nullable = false, updatable = false)
    private FlashcardReviewState reviewState;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false, length = 10, updatable = false)
    private ReviewRating rating;

    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private Instant reviewedAt;

    @Column(name = "previous_due_at", updatable = false)
    private Instant previousDueAt;

    @Column(name = "next_due_at", nullable = false, updatable = false)
    private Instant nextDueAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_before", nullable = false, length = 20, updatable = false)
    private ReviewStage stageBefore;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_after", nullable = false, length = 20, updatable = false)
    private ReviewStage stageAfter;

    @Column(name = "card_data_before", columnDefinition = "text", updatable = false)
    private String cardDataBefore;

    @Column(name = "card_data_after", nullable = false, columnDefinition = "text", updatable = false)
    private String cardDataAfter;

    @Column(name = "algorithm_version", nullable = false, length = 20, updatable = false)
    private String algorithmVersion;

    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;
}
