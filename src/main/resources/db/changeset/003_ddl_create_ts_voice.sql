--liquibase formatted sql
--changeset parsentev:ts_voice

CREATE TABLE ts_voice (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES ts_user,
    text TEXT NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    lang VARCHAR(10) NOT NULL,
    duration INT NOT NULL
);

--rollback DROP TABLE ts_voice;

CREATE TABLE ts_user_vocabulary (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES ts_user,
    word VARCHAR(255) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    total INT,
    lang VARCHAR(10) NOT NULL
);

--rollback DROP TABLE ts_user_vocabulary;

ALTER TABLE ts_user_vocabulary
ADD CONSTRAINT ts_user_vocabulary_unique UNIQUE (user_id, word, lang);

--rollback ALTER TABLE ts_user_vocabulary DROP CONSTRAINT ts_user_vocabulary_unique;

CREATE INDEX idx_ts_user_vocabulary_user_lang ON ts_user_vocabulary(user_id, word, lang);

--rollback DROP INDEX idx_ts_user_vocabulary_user_lang;

CREATE TABLE ts_user_statistic (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES ts_user(id),
    vocabulary_size INT NOT NULL,
    spent_time INT NOT NULL,
    lang VARCHAR(10) NOT NULL
);

--rollback DROP TABLE ts_user_statistic;

ALTER TABLE ts_user_statistic
ADD CONSTRAINT ts_user_statistic_unique UNIQUE (user_id, lang);

--rollback ALTER TABLE ts_user_vocabulary DROP CONSTRAINT ts_user_statistic_unique;

CREATE INDEX idx_ts_user_statistic_lang ON ts_user_statistic(user_id, lang);

--rollback DROP INDEX idx_ts_user_statistic_lang;
