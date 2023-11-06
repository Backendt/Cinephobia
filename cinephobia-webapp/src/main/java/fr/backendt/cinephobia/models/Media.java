package fr.backendt.cinephobia.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.backendt.cinephobia.configurations.TheMovieDBConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.net.URI;

@Data
@Generated
@AllArgsConstructor
@NoArgsConstructor
public class Media {

    @JsonProperty("id")
    private Long id;

    @JsonIgnore
    private MediaType type;

    @JsonProperty("title")
    @JsonAlias("name")
    private String title;

    @JsonProperty("overview")
    private String description;

    @Nullable
    @JsonProperty("poster_path")
    private String posterPath;

    @Nullable
    public String getImageUrl() {
        if(posterPath == null) return null;
        return URI.create(TheMovieDBConfig.IMAGE_BASE_URL)
                .resolve('.' + posterPath)
                .toString();
    }

    public String getMediaUri() {
        return URI.create("/media/")
                .resolve(type.name().toLowerCase() + '/')
                .resolve(String.valueOf(id))
                .toString();
    }

    public Media(Media media) {
        this.id = media.id;
        this.type = media.type;
        this.title = media.title;
        this.description = media.description;
        this.posterPath = media.posterPath;
    }
}
