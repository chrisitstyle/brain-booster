CREATE TABLE user_starred_flashcard
(
    user_id      BIGINT    NOT NULL,
    flashcard_id BIGINT    NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, flashcard_id),

    CONSTRAINT fk_user_starred_flashcard_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_starred_flashcard_flashcard
        FOREIGN KEY (flashcard_id)
            REFERENCES flashcard (flashcard_id)
            ON DELETE CASCADE
);

CREATE INDEX idx_user_starred_flashcard_user_id
    ON user_starred_flashcard (user_id);

CREATE INDEX idx_user_starred_flashcard_flashcard_id
    ON user_starred_flashcard (flashcard_id);