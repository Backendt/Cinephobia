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
import java.util.Optional;

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

    public Optional<String> getImageUrl() {
        return Optional.ofNullable(posterPath)
                .map(imagePath -> URI.create(TheMovieDBConfig.IMAGE_BASE_URL)
                        .resolve(posterPath)
                        .toString());
    }

    public String getMediaUri() {
        return URI.create("/media")
                .resolve(type.name().toLowerCase())
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
