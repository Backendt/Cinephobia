package fr.backendt.cinephobia.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@Generated
@AllArgsConstructor
@NoArgsConstructor
public class TriggerDTO {

    private Long id;

    @Size(min = 2, max = 30, message = "The name must be between 2 and 30 characters")
    @NotNull(message = "The name is required", groups = {NotNull.class})
    private String name;

    @Size(min = 2, max = 60, message = "The description must be between 2 and 60 characters")
    @NotNull(message = "The description is required", groups = {NotNull.class})
    private String description;

    public TriggerDTO(TriggerDTO trigger) {
        this.id = trigger.id;
        this.name = trigger.name;
        this.description = trigger.description;
    }

}
