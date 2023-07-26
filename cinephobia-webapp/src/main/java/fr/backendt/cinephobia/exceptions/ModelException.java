package fr.backendt.cinephobia.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ModelException extends RuntimeException {

    public ModelException(String message) {
        super(message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ModelNotFoundException extends ModelException {

        public ModelNotFoundException(String message) {
            super(message);
        }

    }

}
