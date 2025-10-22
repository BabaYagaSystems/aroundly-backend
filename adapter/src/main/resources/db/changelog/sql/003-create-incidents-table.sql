-- ===========================
-- INCIDENT
-- ===========================
CREATE TABLE IF NOT EXISTS incidents (
                                         id           BIGINT NOT NULL PRIMARY KEY,
                                         title TEXT,
                                         description TEXT,
                                         client_id   BIGINT,
                                         location_id BIGINT,
                                         time_posted  TIMESTAMP,
                                         range        INT,
                                         confirms     INT,
                                         denies       INT,

                                         CONSTRAINT FK_INCIDENT_CLIENT FOREIGN KEY (client_id) REFERENCES clients (id),
                                         CONSTRAINT FK_INCIDENT_LOCATION FOREIGN KEY (location_id) REFERENCES locations (id)
    );
