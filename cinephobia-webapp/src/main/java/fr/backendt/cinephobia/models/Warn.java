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
    @JoinColumn(name = "triggr_id")
    @NotNull(message = "The trigger is required")
    private Trigger trigger;

    @ManyToOne
    @NotNull(message = "The media is required")
    private Media media;

    @NotNull(message = "The exposition level is required")
    @Range(min = 1, max = 10, message = "The exposition level must be between 0 and 10")
    private int expositionLevel;

    public Warn(Trigger trigger, Media media, int expositionLevel) {
        this.trigger = trigger;
        this.media = media;
        this.expositionLevel = expositionLevel;
    }

    public Warn(Warn warn) {
        this.id = warn.id;
        this.trigger = warn.trigger;
        this.media = warn.media;
        this.expositionLevel = warn.expositionLevel;
    }
}
