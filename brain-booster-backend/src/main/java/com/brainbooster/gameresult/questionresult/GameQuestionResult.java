package com.brainbooster.gameresult.questionresult;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.gameresult.GameQuestionType;
import com.brainbooster.gameresult.QuestionAnswerSide;
import com.brainbooster.gameresult.attempt.GameAttempt;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "game_question_results", indexes = {
        @Index(
                name = "idx_game_question_results_attempt_id",
                columnList = "attempt_id"
        ),
        @Index(
                name = "idx_game_question_results_attempt_order",
                columnList = "attempt_id, question_order"
        ),
        @Index(
                name = "idx_game_question_results_flashcard_id",
                columnList = "flashcard_id"
        ),
        @Index(
                name = "idx_game_question_results_flashcard_correct",
                columnList = "flashcard_id, was_correct"
        ),
        @Index(
                name = "idx_game_question_results_question_type",
                columnList = "question_type"
        )
}
)
public class GameQuestionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private GameAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(name = "question_key", length = 120)
    private String questionKey;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 50)
    private GameQuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_with", length = 50)
    private QuestionAnswerSide answerWith;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "was_correct", nullable = false)
    private Boolean wasCorrect;

    @Builder.Default
    @Column(name = "mistakes_count", nullable = false)
    private Integer mistakesCount = 0;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    @PrePersist
    void prePersist() {
        if (mistakesCount == null) {
            mistakesCount = 0;
        }

        if (answeredAt == null) {
            answeredAt = Instant.now();
        }
    }
}
