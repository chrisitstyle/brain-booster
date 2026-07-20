-- clear all tables before inserting fresh test data to prevent unique constraint violations
TRUNCATE TABLE
    "user",
    FLASHCARD_SET,
    FOLDER,
    FOLDER_FLASHCARD_SET,
    FLASHCARD,
    USER_STARRED_FLASHCARD
RESTART IDENTITY CASCADE;

-- add users
INSERT INTO "user" (user_id, nickname, email, password, role, created_at)
VALUES (1, 'testadmin', 'testadmin123@test.com', '$2a$10$LV8wtoOAffLu7OuBIo/5EelC7B3xz2Du8v9nneLRWDYxOXalh1sPa', 'ADMIN', '2024-11-14 18:54:04.887255');

INSERT INTO "user" (user_id, nickname, email, password, role, created_at)
VALUES (2, 'johndoe', 'johndoe@example.com', '$2a$10$LV8wtoOAffLu7OuBIo/5EelC7B3xz2Du8v9nneLRWDYxOXalh1sPa', 'USER', '2024-11-07 13:35:29.346632');

-- add flashcard sets
INSERT INTO FLASHCARD_SET (set_id, user_id, set_name, description, created_at)
VALUES (1, 2, 'Johndoe Set One', 'First set description', '2024-10-10 19:32:38.757753');

INSERT INTO FLASHCARD_SET (set_id, user_id, set_name, description, created_at)
VALUES (2, 2, 'Johndoe Set Two', 'Second set description', '2024-11-13 11:31:24.421905');

-- add folders
INSERT INTO FOLDER (folder_id, user_id, name, description, created_at)
VALUES (1, 2, 'Johndoe Folder One', 'First folder description', '2024-12-01 10:00:00');

INSERT INTO FOLDER (folder_id, user_id, name, description, created_at)
VALUES (2, 2, 'Johndoe Folder Two', 'Second folder description', '2024-12-02 10:00:00');

-- link folders to sets
INSERT INTO FOLDER_FLASHCARD_SET (folder_id, set_id)
VALUES (1, 1),
       (1, 2),
       (2, 2);

-- add flashcards
INSERT INTO FLASHCARD (flashcard_id, set_id, term, definition)
VALUES (1, 1, 'S1-Term 1', 'Definition 1'),
       (2, 1, 'S1-Term 2', 'Definition 2'),
       (3, 1, 'S1-Term 3', 'Definition 3'),
       (4, 1, 'S1-Term 4', 'Definition 4'),
       (5, 1, 'S1-Term 5', 'Definition 5'),
       (6, 1, 'S1-Term 6', 'Definition 6'),
       (7, 1, 'S1-Term 7', 'Definition 7'),
       (8, 1, 'S1-Term 8', 'Definition 8'),
       (9, 1, 'S1-Term 9', 'Definition 9'),
       (10, 1, 'S1-Term 10', 'Definition 10');

INSERT INTO FLASHCARD (flashcard_id, set_id, term, definition)
VALUES (11, 2, 'S2-Term 1', 'Definition 1'),
       (12, 2, 'S2-Term 2', 'Definition 2'),
       (13, 2, 'S2-Term 3', 'Definition 3'),
       (14, 2, 'S2-Term 4', 'Definition 4'),
       (15, 2, 'S2-Term 5', 'Definition 5'),
       (16, 2, 'S2-Term 6', 'Definition 6'),
       (17, 2, 'S2-Term 7', 'Definition 7'),
       (18, 2, 'S2-Term 8', 'Definition 8'),
       (19, 2, 'S2-Term 9', 'Definition 9'),
       (20, 2, 'S2-Term 10', 'Definition 10');

-- add stars
INSERT INTO USER_STARRED_FLASHCARD (user_id, flashcard_id, created_at)
VALUES (2, 1, CURRENT_TIMESTAMP);

-- restarting id sequence
ALTER TABLE "user" ALTER COLUMN user_id RESTART WITH 100;
ALTER TABLE FLASHCARD_SET ALTER COLUMN set_id RESTART WITH 100;
ALTER TABLE FLASHCARD ALTER COLUMN flashcard_id RESTART WITH 100;
ALTER TABLE FOLDER ALTER COLUMN folder_id RESTART WITH 100;