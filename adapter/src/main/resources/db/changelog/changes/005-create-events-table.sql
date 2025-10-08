-- ===========================
-- EVENT
-- ===========================
CREATE TABLE IF NOT EXISTS events (
                                      id           BIGINT NOT NULL PRIMARY KEY,
                                      happening_id BIGINT,
                                      start_time   TIMESTAMP,
                                      end_time     TIMESTAMP,
                                      CONSTRAINT FK_EVENT_HAPPENING FOREIGN KEY (happening_id) REFERENCES happenings (id)
    );

ALTER TABLE events OWNER TO root;

CREATE SEQUENCE event_id_seq;
ALTER SEQUENCE event_id_seq OWNER TO root;

