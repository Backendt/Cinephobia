package fr.backendt.cinephobia.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Generated
@AllArgsConstructor
@NoArgsConstructor
public class MediaDTO {

    private Long id;

    @NotNull(message = "The title is required", groups = {NotNull.class})
    @Size(min = 2, max = 30, message = "The title must be between 2 and 30 characters")
    private String title;

    @URL(protocol = "https", message = "The image url must be a valid https url")
    @NotNull(message = "The image url is required", groups = {NotNull.class})
    private String imageUrl;

}
