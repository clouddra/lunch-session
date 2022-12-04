CREATE TABLE IF NOT EXISTS lunch_sessions
(
    id                IDENTITY PRIMARY KEY,
    `name`            VARCHAR(255)                  NOT NULL,
    status            VARCHAR(255) DEFAULT 'voting' NOT NULL,
    creator           BIGINT                        NOT NULL,
    chosen_restaurant VARCHAR(255),
    foreign key (creator) references lunch_users (id),
    constraint creator_session_name unique (`name`, creator)
);

