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
public class Trigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 2, max = 30, message = "The name must be between 2 and 30 characters")
    @NotBlank(message = "The name is required")
    private String name;

    @Size(min = 2, max = 60, message = "The description must be between 2 and 60 characters")
    @NotBlank(message = "The description is required")
    private String description;

    public Trigger(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
