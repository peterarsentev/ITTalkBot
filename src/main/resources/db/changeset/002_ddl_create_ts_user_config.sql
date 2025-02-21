--liquibase formatted sql
--changeset parsentev:ts_user_config
CREATE TABLE ts_user_config (
    id SERIAL PRIMARY KEY,
    user_id BIGINT not null REFERENCES ts_user,
    key INT not null,
    value VARCHAR(2000)
);

--rollback DROP TABLE ts_user_config;

ALTER TABLE ts_user_config
ADD CONSTRAINT ts_user_config_key UNIQUE (user_id, key);

--rallback DROP CONSTRAINT ts_user_config_key;