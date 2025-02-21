--liquibase formatted sql
--changeset parsentev:create_ts_daily_aim

CREATE TABLE ts_daily_aim (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL REFERENCES ts_user(id),
  create_date DATE NOT NULL,
  duration INT NOT NULL DEFAULT 5,
  scope INT NOT NULL DEFAULT 0,
  progress_bar_message_id INT NOT NULL,
  CONSTRAINT unique_user_create_date UNIQUE (user_id, create_date)
);

--rollback DELETE FROM ts_daily_aim;