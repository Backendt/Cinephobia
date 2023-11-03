package fr.backendt.cinephobia.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class Media {

    private Long id;

    private String title;

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
