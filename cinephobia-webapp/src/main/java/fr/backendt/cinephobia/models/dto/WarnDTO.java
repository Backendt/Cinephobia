package fr.backendt.cinephobia.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Generated
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WarnDTO {

    private Long id;

    @NotNull(message = "The trigger is required")
    private Long triggerId;

    @NotNull(message = "The exposition level is required")
    @Range(min = 0, max = 10, message = "The exposition level must be between 0 and 10")
    private int expositionLevel;

}
