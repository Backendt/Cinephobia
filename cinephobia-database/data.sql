CREATE TABLE IF NOT EXISTS triggr(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(60) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users(
    id BIGINT NOT NULL AUTO_INCREMENT,
    display_name VARCHAR(30) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users_triggers(
    user_id BIGINT NOT NULL,
    trigger_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (trigger_id) REFERENCES triggr(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS warn(
    id BIGINT NOT NULL AUTO_INCREMENT,
    trigger_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    media_type VARCHAR(255) NOT NULL,
    exposition_level TINYINT NOT NULL,
    FOREIGN KEY (trigger_id) REFERENCES triggr(id) ON UPDATE CASCADE ON DELETE CASCADE, /* Deleting a trigger deletes warns */
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (id),
    CONSTRAINT UC_Warn UNIQUE (trigger_id, user_id, media_id, media_type)
);

