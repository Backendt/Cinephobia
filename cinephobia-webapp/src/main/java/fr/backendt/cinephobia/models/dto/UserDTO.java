package fr.backendt.cinephobia.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@Generated
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank(message = "The display name is required")
    @Size(min = 2, max = 30, message = "The display name must be between 2 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9À-Ÿ ]*$", message = "The display name should not use special characters")
    private String displayName;

    @NotBlank(message = "The email is required")
    @Email(message = "Not a valid email")
    private String email;

    @NotBlank(message = "The password is required")
    @Size(min = 6, max = 255, message = "The password must be between 6 and 255 characters")
    private String password;

}