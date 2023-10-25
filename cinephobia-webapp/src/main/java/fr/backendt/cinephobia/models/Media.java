package fr.backendt.cinephobia.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

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

    public Media(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public Media(Media media) {
        this.id = media.id;
        this.title = media.title;
        this.imageUrl = media.imageUrl;
    }
}
