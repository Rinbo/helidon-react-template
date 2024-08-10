/*USERS*/
CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    last_login TIMESTAMP WITHOUT TIME ZONE DEFAULT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX users_email_ix ON users (email);


/*AUTHORITIES*/
CREATE TABLE authorities
(
    user_id   SERIAL      NOT NULL,
    authority VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX authorities_user_id_ix on authorities (user_id, authority);


/*LOGIN PASSCODE*/
CREATE TABLE login_passcode
(
    id       SERIAL PRIMARY KEY,
    passcode CHAR(6)             NOT NULL,
    email    VARCHAR(255) UNIQUE NOT NULL,
    expiry   BIGINT              NOT NULL,
    attempts SMALLINT            NOT NULL DEFAULT 0
);

CREATE INDEX lp_expiry_ix ON login_passcode (expiry);
CREATE INDEX lp_email_ix ON login_passcode (email);


/*SESSIONS*/
CREATE TABLE sessions
(
    id            CHAR(36) PRIMARY KEY,
    user_id       SERIAL                      NOT NULL,
    expires       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_agent    VARCHAR(255)                NOT NULL,
    last_accessed TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX sessions_expires_ix on sessions (expires);


/*SCHEDULE LOCK*/
CREATE TABLE schedule_lock
(
    id   SERIAL PRIMARY KEY,
    type VARCHAR(64) NOT NULL
);

INSERT INTO schedule_lock (type) VALUES ('SESSIONS'), ('PASSCODES');