-- ===========================
-- LOCATION
-- ===========================
CREATE TABLE IF NOT EXISTS locations (
                                         id           BIGINT NOT NULL PRIMARY KEY,
                                         lat          DOUBLE PRECISION NOT NULL,
                                         lng          DOUBLE PRECISION NOT NULL,
                                         address_text VARCHAR(500)
    );

CREATE SEQUENCE location_id_seq;

