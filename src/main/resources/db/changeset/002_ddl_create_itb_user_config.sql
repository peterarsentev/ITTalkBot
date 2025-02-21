--liquibase formatted sql
--changeset parsentev:itb_user_config
CREATE TABLE itb_user_config (
    id SERIAL PRIMARY KEY,
    user_id BIGINT not null REFERENCES itb_user,
    key INT not null,
    value VARCHAR(2000)
);

--rollback DROP TABLE itb_user_config;

ALTER TABLE itb_user_config
ADD CONSTRAINT itb_user_config_key UNIQUE (user_id, key);

--rallback DROP CONSTRAINT itb_user_config_key;