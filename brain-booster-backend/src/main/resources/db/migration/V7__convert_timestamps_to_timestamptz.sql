/*
 * Converts all timestamp columns that represent real moments in time
 * to TIMESTAMPTZ so they map cleanly to java.time.Instant.
 *
 * IMPORTANT:
 * AT TIME ZONE 'UTC' means existing timestamp values are interpreted as UTC.
 * If your existing data was created in another timezone and you want to preserve
 * that local meaning, replace 'UTC' with that timezone, for example 'Europe/Warsaw'.
 */

ALTER TABLE "user"
ALTER COLUMN created_at TYPE TIMESTAMPTZ
    USING created_at AT TIME ZONE 'UTC';

ALTER TABLE flashcard_set
ALTER COLUMN created_at TYPE TIMESTAMPTZ
    USING created_at AT TIME ZONE 'UTC';

ALTER TABLE folder
ALTER COLUMN created_at TYPE TIMESTAMPTZ
    USING created_at AT TIME ZONE 'UTC';

ALTER TABLE user_starred_flashcard
ALTER COLUMN created_at TYPE TIMESTAMPTZ
    USING created_at AT TIME ZONE 'UTC';

ALTER TABLE game_results
ALTER COLUMN completed_at TYPE TIMESTAMPTZ
    USING completed_at AT TIME ZONE 'UTC';

ALTER TABLE game_attempts
ALTER COLUMN completed_at TYPE TIMESTAMPTZ
    USING completed_at AT TIME ZONE 'UTC';

ALTER TABLE game_question_results
ALTER COLUMN answered_at TYPE TIMESTAMPTZ
    USING answered_at AT TIME ZONE 'UTC';