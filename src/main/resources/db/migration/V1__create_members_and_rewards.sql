CREATE TABLE members (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100)             NOT NULL,
    email      VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_members_email UNIQUE (email)
);

CREATE TABLE rewards (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id     BIGINT                   NOT NULL,
    point_type_id INT                      NOT NULL,
    points        BIGINT                   NOT NULL,
    description   VARCHAR(255),
    event_date    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_rewards_member FOREIGN KEY (member_id) REFERENCES members (id)
);

CREATE INDEX idx_rewards_member_id ON rewards (member_id);
