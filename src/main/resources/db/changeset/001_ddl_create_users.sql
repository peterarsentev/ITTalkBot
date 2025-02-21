--liquibase formatted sql
--changeset parsentev:create_users_table
CREATE TABLE ts_user (
    id SERIAL PRIMARY KEY,
    chat_id BIGINT not null,
    client_id BIGINT not null UNIQUE,
    name VARCHAR(2000)
);

--rollback DROP TABLE ts_user;