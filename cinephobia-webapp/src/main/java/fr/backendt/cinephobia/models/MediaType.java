package fr.backendt.cinephobia.models;

import java.util.Optional;

public enum MediaType {
    MOVIE,
    TV;

    public static Optional<MediaType> fromName(String name) {
        name = name.toUpperCase();
        try {
            MediaType type = MediaType.valueOf(name);
            return Optional.of(type);
        } catch(IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
