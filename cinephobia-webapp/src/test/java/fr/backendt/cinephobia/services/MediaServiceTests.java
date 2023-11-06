package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.tmdb.SearchResults;
import fr.backendt.cinephobia.repositories.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class MediaServiceTests {

    private MediaService service;
    private MediaRepository repository;

    private Media movie;
    private Media series;

    @BeforeEach
    void initTests() {
        repository = Mockito.mock(MediaRepository.class);
        service = new MediaService(repository);

        series = new Media(4321L, MediaType.TV, "4321 Series", "The 4321 series", "https://4312.com/poster");
        movie = new Media(1234L, MediaType.MOVIE, "1234 Movie", "The 1234 Movie", "https://1234.com/poster");
    }

    @Test
    void getMovieTest() {
        // GIVEN
        long movieId = 1L;

        Media result;

        when(repository.getMovie(any())).thenReturn(Optional.of(movie));
        // WHEN
        result = service.getMovie(movieId).join();

        // THEN
        verify(repository).getMovie(movieId);
        assertThat(result).isEqualTo(movie);
    }

    @Test
    void getUnknownMovieTest() {
        // GIVEN
        long movieId = 1L;

        when(repository.getMovie(any())).thenReturn(Optional.empty());
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getMovie(movieId).join())
                .withCauseExactlyInstanceOf(EntityException.EntityNotFoundException.class);

        // THEN
        verify(repository).getMovie(movieId);
    }

    @Test
    void getSeriesTest() {
        // GIVEN
        long seriesId = 1L;

        Media result;

        when(repository.getSeries(any())).thenReturn(Optional.of(series));
        // WHEN
        result = service.getSeries(seriesId).join();

        // THEN
        verify(repository).getSeries(seriesId);
        assertThat(result).isEqualTo(series);
    }

    @Test
    void getUnknownSeriesTest() {
        // GIVEN
        long seriesId = 1L;

        when(repository.getSeries(any())).thenReturn(Optional.empty());
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getSeries(seriesId).join())
                .withCauseExactlyInstanceOf(EntityException.EntityNotFoundException.class);

        // THEN
        verify(repository).getSeries(seriesId);
    }

    @Test
    void getMediasTest() {
        // GIVEN
        String mediaTitlePart = "my search";
        int page = 1;

        List<Media> searchMedias = List.of(movie, series);
        SearchResults searchResults = new SearchResults(page, page, 2, searchMedias);
        SearchResults result;

        when(repository.searchMedias(any(), anyInt())).thenReturn(searchResults);
        // WHEN
        result = service.getMedias(mediaTitlePart, page).join();

        // THEN
        verify(repository).searchMedias(mediaTitlePart, page);
        verify(repository, never()).getPopularMovies(anyInt());
        assertThat(result).isEqualTo(searchResults);
    }

    @Test
    void getMediasWithoutSearchTest() {
        // GIVEN
        String mediaTitlePart = "  ";
        int page = 1;

        List<Media> searchMedias = List.of(movie);
        SearchResults searchResults = new SearchResults(page, page, 1, searchMedias);
        SearchResults result;

        when(repository.getPopularMovies(anyInt())).thenReturn(searchResults);
        // WHEN
        result = service.getMedias(mediaTitlePart, page).join();

        // THEN
        verify(repository).getPopularMovies(page);
        verify(repository, never()).searchMedias(any(), anyInt());
        assertThat(result).isEqualTo(searchResults);
    }

}
