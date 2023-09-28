package fr.backendt.cinephobia.models.dto;

import jakarta.validation.constraints.*;

import static fr.backendt.cinephobia.utils.NotBlankIfPresentValidator.NotBlankIfPresent;

public record UserDTO(

    @NotBlankIfPresent(message = "The display name is required")
    @NotNull(message = "The display name is required", groups = {NotNull.class})
    @Size(min = 2, max = 30, message = "The display name must be between 2 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9À-Ÿ ]*$", message = "The display name should not use special characters")
    String displayName,

    @NotBlankIfPresent(message = "The email is required")
    @NotNull(message = "The email is required", groups = {NotNull.class})
    @Email(message = "Not a valid email")
    String email,

    @NotBlankIfPresent(message = "The password is required")
    @NotNull(message = "The password is required", groups = {NotNull.class})
    @Size(min = 6, max = 255, message = "The password must be between 6 and 255 characters")
    String password

) {}
