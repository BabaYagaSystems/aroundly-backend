-- ===========================
-- MEDIA
-- ===========================
CREATE TABLE IF NOT EXISTS medias (
                                         id           BIGINT NOT NULL PRIMARY KEY,
                                         key          VARCHAR(255) NOT NULL,
                                         content_type  VARCHAR(255),
                                         size BIGINT NOT NULL,
                                         created_at TIMESTAMP,
                                         address_text VARCHAR(500),
                                         happening_id BIGINT NOT NULL,
                                         CONSTRAINT FK_MEDIA_HAPPENING FOREIGN KEY (happening_id) REFERENCES happenings (id)
    );

CREATE SEQUENCE media_id_seq;
