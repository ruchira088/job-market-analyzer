CREATE TABLE linkedin_credentials(
    user_id VARCHAR(127),
    created_at TIMESTAMP NOT NULL,
    encrypted_email VARCHAR(255) NOT NULL,
    encrypted_password VARCHAR(255) NOT NULL,

    PRIMARY KEY (user_id),
    CONSTRAINT fk_linkedin_credentials_user_id FOREIGN KEY (user_id) REFERENCES api_user(id)
);