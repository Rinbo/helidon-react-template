CREATE TABLE schedule_lock (
    id SERIAL PRIMARY KEY,
    type VARCHAR(64) NOT NULL
);

INSERT INTO schedule_lock (type) VALUES ('SESSIONS'), ('PASSCODES');