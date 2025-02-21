--liquibase formatted sql
--changeset parsentev:create_itb_users_table
CREATE TABLE itb_user (
    id SERIAL PRIMARY KEY,
    chat_id BIGINT not null,
    client_id BIGINT not null UNIQUE,
    name VARCHAR(2000)
);

--rollback DROP TABLE ts_user;