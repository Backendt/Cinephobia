package fr.backendt.cinephobia.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EntityException extends RuntimeException {

    public EntityException(String message) {
        super(message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class EntityNotFoundException extends EntityException {

        public EntityNotFoundException(String message) {
            super(message);
        }

    }

}
