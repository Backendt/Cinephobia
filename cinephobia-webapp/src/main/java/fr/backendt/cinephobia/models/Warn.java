package fr.backendt.cinephobia.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull(message = "The user is required")
    private User user;

    @NotNull(message = "The media id is required")
    @Positive(message = "The media id must be a positive number")
    private Long mediaId;

    @NotNull(message = "The media type is required")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @NotNull(message = "The exposition level is required")
    @Range(min = 0, max = 10, message = "The exposition level must be between 0 and 10")
    private int expositionLevel;

    public Warn(Trigger trigger, User user, Media media, int expositionLevel) {
        this.trigger = new Trigger(trigger);
        this.user = new User(user);
        this.mediaId = media.getId();
        this.mediaType = media.getType();
        this.expositionLevel = expositionLevel;
    }

    public Warn(Trigger trigger, User user, Long mediaId, MediaType mediaType, int expositionLevel) {
        if(trigger != null) this.trigger = new Trigger(trigger);
        if(user != null) this.user = new User(user);
        this.mediaId = mediaId;
        this.mediaType = mediaType;
        this.expositionLevel = expositionLevel;
    }

    public Warn(Warn warn) {
        this.id = warn.id;
        if(warn.trigger != null) this.trigger = new Trigger(warn.trigger);
        if(warn.user != null) this.user = new User(warn.user);
        this.mediaId = warn.mediaId;
        this.mediaType = warn.mediaType;
        this.expositionLevel = warn.expositionLevel;
    }
}
