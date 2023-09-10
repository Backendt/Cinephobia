package fr.backendt.cinephobia.models.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record WarnDTO (

    Long id,

    @NotNull(message = "The media is required")
    Long mediaId,

    @NotNull(message = "The trigger is required")
    Long triggerId,

    @NotNull(message = "The exposition level is required")
    @Range(min = 1, max = 10, message = "The exposition level must be between 0 and 10")
    int expositionLevel
) {}
