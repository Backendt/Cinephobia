package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.tmdb.SearchResults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MediaRepository {

    private static final Duration TIMEOUT_DURATION = Duration.ofSeconds(10);

    private final String token;

    private final WebClient client;

    public MediaRepository(@Value("${cinephobia.tmdb.jwt}") String token, WebClient tmdbAPI) {
        this.token = token;
        this.client = tmdbAPI;
        validateToken();
    }

    private void initHeaders(HttpHeaders headers) {
        headers.setBearerAuth(token);
        headers.setAccept(
                List.of(org.springframework.http.MediaType.APPLICATION_JSON)
        );
    }

    private void validateToken() {
        RuntimeException invalidTokenException = new IllegalArgumentException("Invalid TMDB JWT Token");
        if(token == null || token.isBlank()) {
            throw invalidTokenException;
        }
        client.get()
                .uri("/authentication")
                .headers(this::initHeaders)
                .retrieve()
                .onStatus(code -> code.isSameCodeAs(HttpStatus.FORBIDDEN), response -> Mono.error(invalidTokenException))
                .toBodilessEntity()
                .block(TIMEOUT_DURATION);
    }

    public Optional<Media> getMovie(Long id) {
        return client.get()
                .uri("/movie/{id}", id)
                .headers(this::initHeaders)
                .retrieve()
                .bodyToMono(Media.class)
                .onErrorResume(error -> Mono.empty())
                .blockOptional(TIMEOUT_DURATION)
                .map(media -> {
                    media.setType(MediaType.MOVIE);
                    return media;
                });
    }

    public Optional<Media> getSeries(Long id) {
        return client.get()
                .uri("/tv/{id}", id)
                .headers(this::initHeaders)
                .retrieve()
                .bodyToMono(Media.class)
                .onErrorResume(error -> Mono.empty())
                .blockOptional(TIMEOUT_DURATION)
                .map(media -> {
                    media.setType(MediaType.TV);
                    return media;
                });
    }

    public SearchResults searchMedias(String search, int page) {
        SearchResults moviesSearch = search(MediaType.MOVIE, search, page);
        SearchResults seriesSearch = search(MediaType.TV, search, page);

        List<Media> movies = moviesSearch.getResults();
        List<Media> series = seriesSearch.getResults();

        int moviesSize = movies.size();
        int seriesSize = series.size();
        int mediasSize = Math.max(moviesSize, seriesSize);

        List<Media> medias = new ArrayList<>(moviesSize + seriesSize);
        for(int i=0; i < mediasSize; i++) {
            boolean isPair = i % 2 == 0;

            Media movie = i < movies.size() ? movies.get(i) : null;
            Media serie = i < series.size() ? series.get(i) : null;
            Media media = isPair && movie != null ? movie : serie;
            medias.add(media);
        }

        int totalResults = moviesSearch.getTotalResults() + seriesSearch.getTotalResults();
        int totalPages = Math.max(moviesSearch.getTotalPages(), seriesSearch.getTotalPages());

        return new SearchResults(page, totalPages, totalResults, medias);
    }

    private SearchResults search(MediaType type, String search, int page) {
        String typeName = type.name().toLowerCase();
        SearchResults results = client.get()
                .uri(uri -> uri
                        .pathSegment("search", typeName)
                        .queryParam("query", "{search}")
                        .queryParam("page", "{page}")
                        .build(search, page))
                .headers(this::initHeaders)
                .retrieve()
                .bodyToMono(SearchResults.class)
                .blockOptional(TIMEOUT_DURATION)
                .orElseThrow(() -> new EntityException("Could not search " + typeName));

        List<Media> newResults = results.getResults().stream()
                .map(media -> {
                    media.setType(type);
                    return media;
                }).toList();
        results.setResults(newResults);
        return results;
    }

    public SearchResults getPopularMovies(int page) {
        SearchResults results = client.get()
                .uri("/movie/popular?page={page}", page)
                .headers(this::initHeaders)
                .retrieve()
                .bodyToMono(SearchResults.class)
                .blockOptional(TIMEOUT_DURATION)
                .orElseThrow(() -> new EntityException("Could not get popular movies"));

        List<Media> newResults = results.getResults().stream()
                .map(media -> {
                    media.setType(MediaType.MOVIE);
                    return media;
                })
                .toList();
        results.setResults(newResults);
        return results;
    }

}
