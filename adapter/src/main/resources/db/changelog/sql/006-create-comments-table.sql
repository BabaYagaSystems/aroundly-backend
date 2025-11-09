-- ===========================
-- COMMENT
-- ===========================
CREATE TABLE IF NOT EXISTS comments (
    id              BIGINT NOT NULL PRIMARY KEY,
    incident_id     BIGINT,
    user_uid        VARCHAR(128),
    value           TEXT,
    created_at      TIMESTAMP,

    CONSTRAINT FK_COMMENT_INCIDENT FOREIGN KEY (incident_id) REFERENCES incidents (id),
    CONSTRAINT FK_COMMENT_CLIENT FOREIGN KEY (user_uid) REFERENCES users (firebase_uid)
    );
