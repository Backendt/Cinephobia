package fr.backendt.cinephobia.models.dto;

public record UserResponseDTO(
    Long id,
    String displayName,
    String email
) {}
