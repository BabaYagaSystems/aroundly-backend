-- ===========================
-- REACTION
-- ===========================
CREATE TABLE IF NOT EXISTS reactions (
    id              BIGINT NOT NULL PRIMARY KEY,
    incident_id     BIGINT,
    comment_id      BIGINT,
    user_uid        VARCHAR(128),
    reaction_type   VARCHAR(20) NOT NULL,
    reacted_at      TIMESTAMP   NOT NULL DEFAULT now(),

    CONSTRAINT FK_REACTION_INCIDENT FOREIGN KEY (incident_id) REFERENCES incidents (id),
    CONSTRAINT FK_REACTION_COMMENT FOREIGN KEY (comment_id) REFERENCES comments (id),
    CONSTRAINT FK_REACTION_CLIENT FOREIGN KEY (user_uid) REFERENCES users (firebase_uid)
    );
