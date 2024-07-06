CREATE TABLE sessions
(
    id      UUID PRIMARY KEY ,
    user_id SERIAL                      not null,
    expires TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_accessed TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);