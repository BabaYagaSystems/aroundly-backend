-- ===========================
-- INCIDENT
-- ===========================
CREATE TABLE IF NOT EXISTS incidents (
     id                     BIGINT NOT NULL PRIMARY KEY,
     title                  TEXT,
     description            TEXT,
     user_uid               VARCHAR(128),
     location_id            BIGINT,
     time_posted            TIMESTAMP,
     range                  INT,
     confirms               INT,
     denies                 INT,
     consecutive_denies     INT,
     expires_at             TIMESTAMP,

     CONSTRAINT FK_INCIDENT_USER FOREIGN KEY (user_uid) REFERENCES users (firebase_uid),
     CONSTRAINT FK_INCIDENT_LOCATION FOREIGN KEY (location_id) REFERENCES locations (id)
    );
