-- ===========================
-- INCIDENT
-- ===========================
CREATE TABLE IF NOT EXISTS incidents (
                                         id           BIGINT NOT NULL PRIMARY KEY,
                                         happening_id BIGINT,
                                         time_posted  TIMESTAMP,
                                         range        INT,
                                         confirms     INT,
                                         denies       INT,
                                         CONSTRAINT FK_INCIDENT_HAPPENING FOREIGN KEY (happening_id) REFERENCES happenings (id)
    );
