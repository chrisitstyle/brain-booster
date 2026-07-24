-- clean up old game results for user 2 to allow safe, repeatable execution of this R__ script
DELETE FROM game_question_results
WHERE attempt_id IN (
    SELECT attempt_id
    FROM game_attempts
    WHERE user_id = 2
);

DELETE FROM game_attempts
WHERE user_id = 2;

DELETE FROM game_results
WHERE user_id = 2;

DO $$
DECLARE
v_user_id          BIGINT := 2; -- target user: johndoe
    v_set_id           BIGINT;
    v_attempt_id       BIGINT;
    v_mode             VARCHAR;
    v_score            INT;
    v_duration         INT;
    v_completed_at     TIMESTAMPTZ;

    -- variables for an individual question result
    v_question_type    VARCHAR;
    v_answer_with      VARCHAR;
    v_prompt           TEXT;
    v_user_answer      TEXT;
    v_correct_answer   TEXT;
    v_was_correct      BOOLEAN;
    v_mistakes_count   INT;
    v_question_order   INT;
    v_question_key     VARCHAR;
    rec                RECORD;
BEGIN
    -- generate game analytics data for flashcard sets 11, 12, and 13
FOR v_set_id IN 11..13 LOOP

        -- generate 8 game attempts for each set, spread across time
        FOR i IN 1..8 LOOP

            -- simulate different game modes
            v_mode := CASE i % 4
                        WHEN 0 THEN 'MULTIPLE_CHOICE'
                        WHEN 1 THEN 'WRITTEN'
                        WHEN 2 THEN 'MATCHING'
                        ELSE 'CUSTOM_TEST'
END;

            -- simulate learning progress over time (scores increase from 4 to 10 out of 10)
            v_score := 3 + i;
            IF v_score > 10 THEN
                v_score := 10;
END IF;

            v_duration := 120 - (i * 5);
            v_completed_at := NOW() - (9 - i) * INTERVAL '1 day';

            -- 1. insert the entire game attempt
INSERT INTO game_attempts (
    user_id,
    set_id,
    mode,
    score,
    total_questions,
    duration_seconds,
    completed_at
)
VALUES (
           v_user_id,
           v_set_id,
           v_mode,
           v_score,
           10,
           v_duration,
           v_completed_at
       )
    RETURNING attempt_id INTO v_attempt_id;

-- 2. insert question-level results (question order starts at 0)
v_question_order := 0;

            -- fetch flashcards belonging to the current set to base prompts and answers on real data
FOR rec IN
SELECT flashcard_id, term, definition
FROM flashcard
WHERE set_id = v_set_id
ORDER BY flashcard_id
    LIMIT 10
            LOOP
                -- cycle through supported Java Enum GameQuestionType values
                v_question_type := CASE v_question_order % 4
                                    WHEN 0 THEN 'TRUE_FALSE'
                                    WHEN 1 THEN 'MULTIPLE_CHOICE'
                                    WHEN 2 THEN 'MATCHING'
                                    ELSE 'WRITTEN'
END;

                -- alternate between asking with term or definition
                IF v_question_order % 2 = 0 THEN
                    v_answer_with := 'DEFINITION';
                    v_prompt := rec.term;
                    v_correct_answer := rec.definition;
ELSE
                    v_answer_with := 'TERM';
                    v_prompt := rec.definition;
                    v_correct_answer := rec.term;
END IF;

                -- determine if this answer represents a success or an error
                v_was_correct := v_question_order < v_score;

                -- build realistic answers matching frontend logic
                IF v_question_type = 'TRUE_FALSE' THEN
                    v_correct_answer := 'True';
                    v_prompt := rec.term || ' = ' || rec.definition;

                    IF v_was_correct THEN
                        v_user_answer := 'True';
                        v_mistakes_count := 0;
ELSE
                        v_user_answer := 'False';
                        v_mistakes_count := 1;
END IF;
ELSE
                    IF v_was_correct THEN
                        v_user_answer := v_correct_answer;
                        v_mistakes_count := 0;
ELSE
                        -- for incorrect answers, pick ANOTHER random term/definition from the same set
SELECT CASE WHEN v_answer_with = 'TERM' THEN term ELSE definition END
INTO v_user_answer
FROM flashcard
WHERE set_id = v_set_id AND flashcard_id != rec.flashcard_id
ORDER BY random()
    LIMIT 1;

v_mistakes_count := 1 + (v_question_order % 2); -- simulate 1 or 2 mistakes
END IF;
END IF;

                -- construct unique question key matching frontend format (e.g. multiple_choice-101-0)
                v_question_key := lower(v_question_type) || '-' || rec.flashcard_id || '-' || v_question_order;

INSERT INTO game_question_results (
    attempt_id,
    flashcard_id,
    question_key,
    question_order,
    question_type,
    answer_with,
    prompt,
    user_answer,
    correct_answer,
    was_correct,
    mistakes_count,
    answered_at
)
VALUES (
           v_attempt_id,
           rec.flashcard_id,
           v_question_key,
           v_question_order,
           v_question_type,
           v_answer_with,
           v_prompt,
           v_user_answer,
           v_correct_answer,
           v_was_correct,
           v_mistakes_count,
           v_completed_at - ((10 - v_question_order) * INTERVAL '2 seconds')
    );

v_question_order := v_question_order + 1;
END LOOP;

END LOOP;
END LOOP;
END $$;

-- 3. populate aggregated game results summary table
INSERT INTO game_results (
    user_id,
    set_id,
    mode,
    score,
    total_questions,
    duration_seconds,
    completed_at
)
SELECT
    user_id,
    set_id,
    mode,
    MAX(score),
    10,
    AVG(duration_seconds)::INT,
    MAX(completed_at)
FROM game_attempts
WHERE user_id = 2 AND set_id IN (11, 12, 13)
GROUP BY user_id, set_id, mode;