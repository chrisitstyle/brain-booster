/*
 * Stores question-level results for a completed game attempt.
 *
 * Each row represents how the user answered a specific flashcard-based question.
 * This table enables analytics such as most frequently missed flashcards,
 * correct and incorrect answer counts per flashcard, weak flashcard review,
 * and performance analysis by question type or game mode.
 */
CREATE TABLE game_question_results
(
    question_result_id BIGSERIAL PRIMARY KEY,
    attempt_id         BIGINT      NOT NULL,
    flashcard_id       BIGINT      NOT NULL,
    question_key       VARCHAR(120),
    question_order     INT         NOT NULL,
    question_type      VARCHAR(50) NOT NULL,
    answer_with        VARCHAR(50),
    prompt             TEXT,
    user_answer        TEXT,
    correct_answer     TEXT,
    was_correct        BOOLEAN     NOT NULL,
    mistakes_count     INT         NOT NULL DEFAULT 0,
    answered_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_game_question_results_attempt
        FOREIGN KEY (attempt_id)
            REFERENCES game_attempts (attempt_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_game_question_results_flashcard
        FOREIGN KEY (flashcard_id)
            REFERENCES flashcard (flashcard_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_game_question_results_question_order_non_negative
        CHECK (question_order >= 0),

    CONSTRAINT chk_game_question_results_mistakes_non_negative
        CHECK (mistakes_count >= 0)
);

CREATE INDEX idx_game_question_results_attempt_id
    ON game_question_results (attempt_id);

CREATE INDEX idx_game_question_results_attempt_order
    ON game_question_results (attempt_id, question_order);

CREATE INDEX idx_game_question_results_flashcard_id
    ON game_question_results (flashcard_id);

CREATE INDEX idx_game_question_results_flashcard_correct
    ON game_question_results (flashcard_id, was_correct);

CREATE INDEX idx_game_question_results_question_type
    ON game_question_results (question_type);