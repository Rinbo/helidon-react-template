CREATE TABLE login_passcode
(
    id       SERIAL PRIMARY KEY,
    passcode CHAR(6)      NOT NULL,
    email    VARCHAR(255) UNIQUE NOT NULL,
    expiry   BIGINT       NOT NULL,
    attempts SMALLINT     NOT NULL DEFAULT 0
);

CREATE INDEX expiry_ix ON login_passcode (expiry);

