/*
 * Stores every completed game attempt.
 *
 * This table is used as a historical log of user gameplay.
 * Unlike game_results, which stores the latest result per user, set and mode,
 * this table keeps every completed attempt so the application can later show
 * progress over time, attempt history and performance trends.
 */
CREATE TABLE game_attempts
(
    attempt_id       BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL,
    set_id           BIGINT      NOT NULL,
    mode             VARCHAR(50) NOT NULL,
    score            INT         NOT NULL,
    total_questions  INT         NOT NULL,
    duration_seconds INT,
    completed_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_game_attempts_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_game_attempts_flashcard_set
        FOREIGN KEY (set_id)
            REFERENCES flashcard_set (set_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_game_attempts_score_non_negative
        CHECK (score >= 0),

    CONSTRAINT chk_game_attempts_total_questions_positive
        CHECK (total_questions > 0),

    CONSTRAINT chk_game_attempts_score_lte_total
        CHECK (score <= total_questions),

    CONSTRAINT chk_game_attempts_duration_non_negative
        CHECK (duration_seconds IS NULL OR duration_seconds >= 0)
);

CREATE INDEX idx_game_attempts_user_id_completed_at
    ON game_attempts (user_id, completed_at DESC);

CREATE INDEX idx_game_attempts_set_id_completed_at
    ON game_attempts (set_id, completed_at DESC);

CREATE INDEX idx_game_attempts_user_set_mode_completed_at
    ON game_attempts (user_id, set_id, mode, completed_at DESC);