CREATE TABLE IF NOT EXISTS triggr(
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
    triggr_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    exposition_level TINYINT NOT NULL,
    FOREIGN KEY (triggr_id) REFERENCES triggr(id),
    FOREIGN KEY (media_id) REFERENCES media(id),
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

CREATE TABLE IF NOT EXISTS users_warns(
    warn_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (warn_id) REFERENCES warn(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO platform(name) VALUES ('Netflix'), ('Prime Video'), ('Disney+'), ('Cinema'), ('Other');
INSERT INTO media(title, image_url) VALUES ('Cinephobia: The Revenge', 'https://example.com/cinephobia.png');
INSERT INTO media_platforms(platform_id, media_id) VALUES (1, 1);
INSERT INTO triggr(name, description) VALUES ('Testphobia', 'Fear of unit tests failing'), ('Bugphobia', 'Fear of software bugs');
INSERT INTO warn(triggr_id, media_id, exposition_level) VALUES (2, 1, 9);
INSERT INTO users(display_name, email, password, role) VALUES ('John Doe', 'john.doe@test.com', 'John1234', 'USER');