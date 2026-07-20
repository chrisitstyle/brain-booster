-- Clear all business tables
-- CASCADE ensures that relations (foreign keys) are removed without throwing errors
-- skip the 'flyway_schema_history' table so Flyway's state remains intact

TRUNCATE TABLE
    "user",
    flashcard_set,
    flashcard,
    folder,
    folder_flashcard_set,
    user_starred_flashcard,
    game_results,
    game_attempts,
    game_question_results
CASCADE;