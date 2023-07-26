INSERT INTO platform(name) VALUES ('Netflix'), ('Prime Video'), ('Disney+'), ('Cinema'), ('Other');
INSERT INTO media(title, image_url) VALUES ('Cinephobia: The Revenge', 'https://example.com/cinephobia.png');
INSERT INTO media_platforms(platform_id, media_id) VALUES (1, 1);
INSERT INTO trigger(name, description) VALUES ('Testphobia', 'Fear of unit tests failing'), ('Bugphobia', 'Fear of software bugs');
INSERT INTO warn(trigger_id, media_id, exposition_level) VALUES (2, 1, 9);
