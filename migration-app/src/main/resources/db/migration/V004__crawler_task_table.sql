CREATE TABLE crawler_task(
    id VARCHAR(127),
    user_id VARCHAR(127) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_crawler_task_user_id FOREIGN KEY (user_id) REFERENCES api_user(user_id)
);