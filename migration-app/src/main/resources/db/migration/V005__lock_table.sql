CREATE TABLE database_lock(
    id VARCHAR(127),
    acquired_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,

    PRIMARY KEY (id)
)