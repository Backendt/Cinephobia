package fr.backendt.cinephobia.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Generated
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The title is required")
    @Size(min = 2, max = 30, message = "The title must be between 2 and 30 characters")
    private String title;

    @URL(protocol = "https", message = "The image url must be a valid https url")
    @NotBlank(message = "The image url is required")
    private String imageUrl;

    @Size(min = 1, max = 10, message = "There must be between 1 and 10 platforms") // TODO Change min to 0
    @ManyToMany
    @JoinTable(inverseJoinColumns = @JoinColumn(name = "platform_id"))
    private List<Platform> platforms;

    public Media(String title, String imageUrl, List<Platform> platforms) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.platforms = platforms;
    }

    public Media(Media media) {
        this.id = media.id;
        this.title = media.title;
        this.imageUrl = media.imageUrl;
        this.platforms = new ArrayList<>(media.platforms);
    }
}
