package fr.backendt.cinephobia.exceptions;

public class EntityException extends RuntimeException {

    public EntityException(String message) {
        super(message);
    }

    public static class EntityNotFoundException extends EntityException {

        public EntityNotFoundException(String message) {
            super(message);
        }

    }

}
