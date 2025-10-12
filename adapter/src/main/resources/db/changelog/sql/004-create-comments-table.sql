-- ===========================
-- COMMENT
-- ===========================
CREATE TABLE IF NOT EXISTS comments (
                                        id           BIGINT NOT NULL PRIMARY KEY,
                                        happening_id BIGINT,
                                        client_id    BIGINT,
                                        value        TEXT,
                                        created_at   TIMESTAMP,
                                        CONSTRAINT FK_COMMENT_HAPPENING FOREIGN KEY (happening_id) REFERENCES happenings (id),
    CONSTRAINT FK_COMMENT_CLIENT FOREIGN KEY (client_id) REFERENCES clients (id)
    );

CREATE SEQUENCE comment_id_seq;

