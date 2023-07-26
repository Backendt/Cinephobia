package fr.backendt.cinephobia.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Generated
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The name is required")
    @Size(min = 2, max = 20, message = "The name must be between 2 and 20 characters")
    private String name;

    public Platform(String name) {
        this.name = name;
    }

    public Platform(Platform platform) {
        this.id = platform.id;
        this.name = platform.name;
    }
}
