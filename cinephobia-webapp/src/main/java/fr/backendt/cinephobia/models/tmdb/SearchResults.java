package fr.backendt.cinephobia.models.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.backendt.cinephobia.models.Media;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Generated
@AllArgsConstructor
@NoArgsConstructor
public class SearchResults {

    @JsonProperty("page")
    private int currentPage;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("total_results")
    private int totalResults;

    @JsonProperty("results")
    private List<Media> results;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SearchResults media = (SearchResults) o;
        return currentPage == media.currentPage && totalPages == media.totalPages && totalResults == media.totalResults && Objects.equals(results, media.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), currentPage, totalPages, totalResults, results);
    }
}
