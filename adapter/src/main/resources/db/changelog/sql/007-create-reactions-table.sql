-- ===========================
-- REACTION
-- ===========================
CREATE TABLE IF NOT EXISTS reactions (
    id           BIGINT NOT NULL PRIMARY KEY,
                                         happening_id BIGINT,
                                         comment_id   BIGINT,
                                         client_id    BIGINT,
                                         likes        INT,
                                         dislikes     INT,
                                         CONSTRAINT FK_REACTION_HAPPENING FOREIGN KEY (happening_id) REFERENCES happenings (id),
    CONSTRAINT FK_REACTION_COMMENT FOREIGN KEY (comment_id) REFERENCES comments (id),
    CONSTRAINT FK_REACTION_CLIENT FOREIGN KEY (client_id) REFERENCES clients (id)
    );
