-- ===========================
-- LOCATION
-- ===========================
CREATE TABLE IF NOT EXISTS locations (
                                         id           BIGINT NOT NULL PRIMARY KEY,
                                         lat          DOUBLE PRECISION NOT NULL,
                                         lng          DOUBLE PRECISION NOT NULL,
                                         address_text VARCHAR(500)
    );

ALTER TABLE locations OWNER TO root;

CREATE SEQUENCE location_id_seq;
ALTER SEQUENCE location_id_seq OWNER TO root;

