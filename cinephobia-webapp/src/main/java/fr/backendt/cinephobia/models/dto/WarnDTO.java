package fr.backendt.cinephobia.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class WarnDTO {

    private Long id;

    @NotNull(message = "The media is required")
    private Long mediaId;

    @NotNull(message = "The trigger is required")
    private Long triggerId;

    @NotNull(message = "The exposition level is required")
    @Range(min = 1, max = 10, message = "The exposition level must be between 0 and 10")
    private int expositionLevel;

    public WarnDTO(Long mediaId, Long triggerId, int expositionLevel) {
        this.mediaId = mediaId;
        this.triggerId = triggerId;
        this.expositionLevel = expositionLevel;
    }
}
