CREATE TABLE IF NOT EXISTS user_participation
(
    participant   BIGINT NOT NULL,
    lunch_session BIGINT NOT NULL,
    restaurant    VARCHAR(255),
    FOREIGN KEY (participant) references lunch_users (id),
    FOREIGN KEY (lunch_session) references lunch_sessions (id),
    CONSTRAINT user_session PRIMARY KEY (participant, lunch_session)
);