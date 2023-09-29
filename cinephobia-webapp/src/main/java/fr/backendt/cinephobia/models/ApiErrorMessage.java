package fr.backendt.cinephobia.models;

import org.springframework.http.HttpStatus;

import java.util.List;

public record ApiErrorMessage (
    HttpStatus status,
    String message,
    List<String> errors
) {}
