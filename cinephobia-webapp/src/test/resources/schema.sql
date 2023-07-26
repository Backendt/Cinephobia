CREATE TABLE IF NOT EXISTS trigger(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(60) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS platform(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS media(
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(30) NOT NULL UNIQUE,
    image_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS media_platforms(
    platform_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    FOREIGN KEY (platform_id) REFERENCES platform(id),
    FOREIGN KEY (media_id) REFERENCES media(id)
);

CREATE TABLE IF NOT EXISTS warn(
    id BIGINT NOT NULL AUTO_INCREMENT,
    trigger_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    exposition_level TINYINT NOT NULL,
    FOREIGN KEY (trigger_id) REFERENCES trigger(id),
    FOREIGN KEY (media_id) REFERENCES media(id),
    PRIMARY KEY (id)
);