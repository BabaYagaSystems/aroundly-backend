-- ===========================
-- HAPPENING
-- ===========================
CREATE TABLE IF NOT EXISTS happenings (
                                          id          BIGINT NOT NULL PRIMARY KEY,
                                          title       VARCHAR(255),
    description TEXT,
    client_id   BIGINT,
    location_id BIGINT,
    image_url   VARCHAR(500),
    created_at  TIMESTAMP,
    CONSTRAINT FK_HAPPENING_CLIENT FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT FK_HAPPENING_LOCATION FOREIGN KEY (location_id) REFERENCES locations (id)
    );

ALTER TABLE happenings OWNER TO root;

CREATE SEQUENCE happening_id_seq;
ALTER SEQUENCE happening_id_seq OWNER TO root;

