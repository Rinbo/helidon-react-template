CREATE TABLE login_passcode
(
    passcode  CHAR(6) PRIMARY KEY NOT NULL,
    email  VARCHAR(255)         NOT NULL,
    expiry BIGINT               NOT NULL
);

CREATE INDEX expiry_ix ON login_passcode (expiry);

