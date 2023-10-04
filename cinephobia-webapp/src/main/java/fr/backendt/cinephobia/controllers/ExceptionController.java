package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.EntityException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(EntityException.class)
    public String catchEntityException(Model model, EntityException exception) {
        model.addAttribute("err", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String catchExceptions(Model model) {
        String errorMessage = "Sorry, an unexpected error occurred! Please try again later";
        model.addAttribute("err", errorMessage);
        return "error";
    }

}
