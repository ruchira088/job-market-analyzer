CREATE TABLE api_user(
    id VARCHAR(127),
    created_at TIMESTAMP NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NULL,

    PRIMARY KEY (id)
);