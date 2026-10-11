--liquibase formatted sql
--changeset dev:002-create-get-load-test-msg
CREATE TABLE get_load_test_msg (
    id            BIGSERIAL PRIMARY KEY,
    projectid     VARCHAR(36) NOT NULL,
    testid        VARCHAR(50) NOT NULL,
    chunk_index   INT,
    total_chunks  INT,
    payload       BYTEA,
    is_complete   BOOL        DEFAULT FALSE,
    status        VARCHAR(20) DEFAULT 'RECEIVED',
    created_at    TIMESTAMPTZ,
    updated_at    TIMESTAMPTZ,
    assembled_at  TIMESTAMPTZ,
    error_msg     TEXT,
    CONSTRAINT uq_get_chunk UNIQUE (testid, chunk_index)
);
CREATE INDEX idx_get_status ON get_load_test_msg(status);
CREATE INDEX idx_get_testid ON get_load_test_msg(testid);
--rollback DROP TABLE get_load_test_msg;
