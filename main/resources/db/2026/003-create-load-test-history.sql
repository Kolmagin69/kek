--liquibase formatted sql
--changeset dev:003-create-load-test-history
CREATE TABLE load_test_history (
    id                       BIGSERIAL PRIMARY KEY,
    projectid                VARCHAR(36),
    testid                   VARCHAR(50),
    userid                   VARCHAR(50),
    total_chunks_sent        INT         DEFAULT 0,
    total_chunks_received    INT         DEFAULT 0,
    sent_status              VARCHAR(20) DEFAULT 'PENDING',
    received_status          VARCHAR(20) DEFAULT 'PENDING',
    overall_status           VARCHAR(20) DEFAULT 'INITIATED',
    msg_size                 BIGINT,
    sent_payload_preview     TEXT,
    received_payload_preview TEXT,
    sent_at                  TIMESTAMPTZ,
    received_at              TIMESTAMPTZ,
    assembled_at             TIMESTAMPTZ,
    error_msg                TEXT,
    created_at               TIMESTAMPTZ,
    updated_at               TIMESTAMPTZ
);
CREATE INDEX idx_hist_testid ON load_test_history(testid);
CREATE INDEX idx_hist_status ON load_test_history(overall_status);
--rollback DROP TABLE load_test_history;
