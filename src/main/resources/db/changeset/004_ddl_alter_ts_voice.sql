--liquibase formatted sql
--changeset parsentev:alter_ts_voice_add_message_id, traslate_text

ALTER TABLE ts_voice ADD COLUMN message_id INT NOT NULL DEFAULT -1;

--rollback ALTER TABLE ts_voice DROP COLUMN message_id;

ALTER TABLE ts_voice ADD COLUMN translate_text TEXT NOT NULL DEFAULT '';

--rollback ALTER TABLE ts_voice DROP COLUMN translate_text;

CREATE INDEX idx_ts_voice_message_id ON ts_voice(message_id);

--rollback DROP INDEX idx_ts_voice_message_id;