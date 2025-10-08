-- ===========================
-- INCIDENT
-- ===========================
CREATE TABLE IF NOT EXISTS incidents (
                                         id           BIGINT NOT NULL PRIMARY KEY,
                                         happening_id BIGINT,
                                         time_posted  TIMESTAMP NOT NULL,
                                         expires_at TIMESTAMP,
                                         range        INT,
                                         confirms     INT,
                                         denies       INT,
                                         CONSTRAINT FK_INCIDENT_HAPPENING FOREIGN KEY (happening_id) REFERENCES happenings (id)
    );

ALTER TABLE incidents OWNER TO root;

CREATE SEQUENCE incident_id_seq;
ALTER SEQUENCE incident_id_seq OWNER TO root;

