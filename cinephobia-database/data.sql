CREATE TABLE IF NOT EXISTS triggr(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(60) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS warn(
    id BIGINT NOT NULL AUTO_INCREMENT,
    triggr_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    exposition_level TINYINT NOT NULL,
    FOREIGN KEY (triggr_id) REFERENCES triggr(id),
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
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (trigger_id) REFERENCES triggr(id)
);

