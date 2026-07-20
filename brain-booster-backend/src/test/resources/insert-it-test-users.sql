TRUNCATE TABLE "user" RESTART IDENTITY CASCADE;
-- add dedicated admin for integration tests
INSERT INTO "user" (user_id, nickname, email, password, role, created_at)
VALUES (1, 'it-admin', 'it-admin@test.com', '$2a$10$LV8wtoOAffLu7OuBIo/5EelC7B3xz2Du8v9nneLRWDYxOXalh1sPa', 'ADMIN', CURRENT_TIMESTAMP);

-- add dedicated user for integration tests
INSERT INTO "user" (user_id, nickname, email, password, role, created_at)
VALUES (2, 'it-user', 'it-user@test.com', '$2a$10$LV8wtoOAffLu7OuBIo/5EelC7B3xz2Du8v9nneLRWDYxOXalh1sPa', 'USER', CURRENT_TIMESTAMP);

-- restart sequence so automatically generated IDs start from 100
ALTER TABLE "user" ALTER COLUMN user_id RESTART WITH 100;