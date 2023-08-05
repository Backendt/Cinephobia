package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.ApiErrorMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class RestExceptionController extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> errorMessages = new ArrayList<>();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        // Field errors
        for(FieldError error : fieldErrors) {
            String errorMessage = "%s: %s".formatted(error.getField(), error.getDefaultMessage());
            errorMessages.add(errorMessage);
        }

        // Global errors
        List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();
        for(ObjectError error : globalErrors) {
            String errorMessage = "%s: %s".formatted(error.getObjectName(), error.getDefaultMessage());
            errorMessages.add(errorMessage);
        }

        String mainMessage = "Invalid fields";
        ApiErrorMessage apiErrorMessage = new ApiErrorMessage(HttpStatus.BAD_REQUEST, mainMessage, errorMessages);
        return handleExceptionInternal(ex, apiErrorMessage, headers, apiErrorMessage.getStatus(), request);
    }
}
