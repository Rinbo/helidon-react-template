CREATE TABLE sessions
(
    id            CHAR(36) PRIMARY KEY,
    user_id       SERIAL                      NOT NULL,
    expires       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_agent    VARCHAR(255)                NOT NULL,
    last_accessed TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX expires_ix on sessions (expires);
