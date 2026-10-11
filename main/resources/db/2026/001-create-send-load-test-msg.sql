--liquibase formatted sql
--changeset dev:001-create-send-load-test-msg
CREATE TABLE send_load_test_msg (
    id            BIGSERIAL PRIMARY KEY,
    testid        VARCHAR(36) NOT NULL,
    projectid     VARCHAR(50) NOT NULL,
    payload       BYTEA       NOT NULL,
    payload_size  INT,
    chunk_index   INT,
    total_chunks  INT         DEFAULT 1,
    status        VARCHAR(20) DEFAULT 'PENDING',
    retry_count   INT         DEFAULT 0,
    created_at    TIMESTAMPTZ,
    updated_at    TIMESTAMPTZ,
    sent_at       TIMESTAMPTZ,
    error_msg     TEXT,
    CONSTRAINT uq_send_chunk UNIQUE (testid, chunk_index)
);
CREATE INDEX idx_send_status ON send_load_test_msg(status);
CREATE INDEX idx_send_testid ON send_load_test_msg(testid);
--rollback DROP TABLE send_load_test_msg;
