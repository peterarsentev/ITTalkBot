--liquibase formatted sql
--changeset parsentev:itb_voice

CREATE TABLE itb_voice (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES itb_user,
    text TEXT NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    duration INT NOT NULL
);

--rollback DROP TABLE itb_voice;
