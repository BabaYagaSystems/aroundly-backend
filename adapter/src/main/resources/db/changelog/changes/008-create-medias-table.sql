-- ===========================
-- MEDIA
-- ===========================
CREATE TABLE IF NOT EXISTS medias (
                                         id           BIGINT NOT NULL PRIMARY KEY,
                                         key          VARCHAR(255) NOT NULL,
                                         contentType  VARCHAR(255),
                                         size BIGINT NOT NULL,
                                         createdAt TIMESTAMP,
                                         address_text VARCHAR(500),
                                         happening_id BIGINT NOT NULL,
                                         CONSTRAINT FK_MEDIA_HAPPENING FOREIGN KEY (happening_id) REFERENCES happenings (id)
    );

ALTER TABLE medias OWNER TO root;

CREATE SEQUENCE media_id_seq;
ALTER SEQUENCE media_id_seq OWNER TO root;