-- ===========================
-- CLIENT
-- ===========================
CREATE TABLE IF NOT EXISTS clients (
                                       id          BIGINT NOT NULL PRIMARY KEY,
                                       foreign_id VARCHAR(255) NOT NULL,
    fcm_token   VARCHAR(255),
    range_km    INT
    );
