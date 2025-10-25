-- ===========================
-- MEDIA
-- ===========================
CREATE TABLE IF NOT EXISTS medias (
                                         id           BIGINT NOT NULL PRIMARY KEY,
                                         incident_id BIGINT NOT NULL,
                                         key          VARCHAR(255) NOT NULL,
                                         content_type  VARCHAR(255),
                                         size BIGINT NOT NULL,
                                         created_at TIMESTAMP,
                                         CONSTRAINT FK_MEDIA_INCIDENT FOREIGN KEY (incident_id) REFERENCES incidents (id)
    );
