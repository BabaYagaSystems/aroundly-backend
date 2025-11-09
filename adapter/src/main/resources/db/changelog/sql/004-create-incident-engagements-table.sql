CREATE TABLE IF NOT EXISTS incident_engagements (

    id               BIGINT       NOT NULL PRIMARY KEY,
    incident_id      BIGINT       NOT NULL,
    user_id          BIGINT       NOT NULL,
    engagement_type  VARCHAR(20)  NOT NULL,
    engaged_at       TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT FK_INCIDENT_ENGAGEMENT_INCIDENT
    FOREIGN KEY (incident_id) REFERENCES incidents (id),
    CONSTRAINT UK_INCIDENT_ENGAGEMENT_UNIQUE
    UNIQUE (incident_id, user_id)
    );
