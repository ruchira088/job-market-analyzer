CREATE TABLE api_user_credentials (
    user_id VARCHAR(127),
    hashed_password VARCHAR(127) NOT NULL,

    PRIMARY KEY (user_id),
    CONSTRAINT fk_api_user_credentials_user_id FOREIGN KEY (user_id) REFERENCES api_user(id)
);