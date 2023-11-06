package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.configurations.TheMovieDBConfig;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.tmdb.SearchResults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MediaRepositoryTests {

    private MediaRepository repository;

    @BeforeEach
    void initTests() {
        String token = System.getenv("TMDB_JWT");
        WebClient tmdbAPI = new TheMovieDBConfig().tmdbAPI();
        repository = new MediaRepository(token, tmdbAPI);
    }

    @Test
    void getMovieTest() {
        // GIVEN
        long mediaId = 507110;

        Media expectedMedia = new Media(mediaId, MediaType.MOVIE, "Java", "An artist realizes the consequences of her artistic obsession.", "/9VrwPnieWbU2eH1gRNl2CBqr8eL.jpg");
        Optional<Media> result;

        // WHEN
        result = repository.getMovie(mediaId);

        // THEN
        assertThat(result)
                .isNotEmpty()
                .contains(expectedMedia);
    }

    @Test
    void getUnknownMovieTest() {
        // GIVEN
        long mediaId = 0;
        Optional<Media> result;
        // WHEN
        result = repository.getMovie(mediaId);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void getSeriesTest() {
        // GIVEN
        long mediaId = 134916;
        Media expectedMedia = new Media(mediaId, MediaType.TV, "Java Development", "For anyone that wanna learn java", null);

        Optional<Media> result;

        // WHEN
        result = repository.getSeries(mediaId);

        // THEN
        assertThat(result)
                .isNotEmpty()
                .contains(expectedMedia);
    }

    @Test
    void getUnknownSeriesTest() {
        // GIVEN
        long mediaId = 0;
        Optional<Media> result;
        // WHEN
        result = repository.getSeries(mediaId);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void searchMediasTest() {
        // GIVEN
        int page = 1;
        String search = "java";

        int expectedResultAmount = 105;
        SearchResults result;
        // WHEN
        result = repository.searchMedias(search, page);

        // THEN
        assertThat(result.getCurrentPage()).isEqualTo(page);
        assertThat(result.getTotalResults()).isGreaterThanOrEqualTo(expectedResultAmount); // Should be equal, but adding a movie would fail the test
        assertThat(result.getResults().get(0)).hasNoNullFieldsOrPropertiesExcept("posterPath");
        assertThat(result.getResults()).doesNotContainNull();
    }

    @Test
    void getPopularMoviesTest() {
        // GIVEN
        int page = 1;
        SearchResults result;
        int minResultSizeExpected = 15;
        // WHEN
        result = repository.getPopularMovies(page);

        // THEN
        assertThat(result).hasNoNullFieldsOrProperties();
        assertThat(result.getCurrentPage()).isEqualTo(page);
        assertThat(result.getResults()).hasSizeGreaterThan(minResultSizeExpected);
        assertThat(result.getResults().get(0)).hasNoNullFieldsOrPropertiesExcept("posterPath");
    }

}
