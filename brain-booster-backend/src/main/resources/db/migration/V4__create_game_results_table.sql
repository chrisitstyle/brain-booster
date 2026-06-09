CREATE TABLE game_results
(
    result_id        BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL,
    set_id           BIGINT      NOT NULL,
    mode             VARCHAR(50) NOT NULL,
    score           INT         NOT NULL,
    total_questions  INT         NOT NULL,
    duration_seconds INT,
    completed_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_game_results_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_game_results_flashcard_set
        FOREIGN KEY (set_id)
            REFERENCES flashcard_set (set_id)
            ON DELETE CASCADE,

    CONSTRAINT uq_game_results_user_set_mode
        UNIQUE (user_id, set_id, mode),

    CONSTRAINT chk_game_results_score_non_negative
        CHECK (score >= 0),

    CONSTRAINT chk_game_results_total_questions_positive
        CHECK (total_questions > 0),

    CONSTRAINT chk_game_results_score_lte_total
        CHECK (score <= total_questions),

    CONSTRAINT chk_game_results_duration_non_negative
        CHECK (duration_seconds IS NULL OR duration_seconds >= 0)
);

CREATE INDEX idx_game_results_user_id_completed_at
    ON game_results (user_id, completed_at DESC);

CREATE INDEX idx_game_results_set_id_completed_at
    ON game_results (set_id, completed_at DESC);