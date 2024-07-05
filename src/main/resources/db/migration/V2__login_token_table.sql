CREATE TABLE login_token
(
    token  CHAR(36) PRIMARY KEY NOT NULL,
    email  VARCHAR(255)         NOT NULL,
    expiry BIGINT               NOT NULL
);

CREATE INDEX expiry_ix ON login_token (expiry);

