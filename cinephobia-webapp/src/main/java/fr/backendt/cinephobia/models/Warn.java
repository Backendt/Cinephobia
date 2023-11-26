package fr.backendt.cinephobia.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class Warn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trigger_id")
    @NotNull(message = "The trigger is required")
    private Trigger trigger;

    @NotNull(message = "The media is required")
    private Long mediaId;

    @NotNull(message = "The exposition level is required")
    @Range(min = 0, max = 10, message = "The exposition level must be between 0 and 10")
    private int expositionLevel;

    public Warn(Trigger trigger, Long mediaId, int expositionLevel) {
        this.trigger = trigger;
        this.mediaId = mediaId;
        this.expositionLevel = expositionLevel;
    }

    public Warn(Warn warn) {
        this.id = warn.id;
        this.trigger = warn.trigger;
        this.mediaId = warn.mediaId;
        this.expositionLevel = warn.expositionLevel;
    }
}
