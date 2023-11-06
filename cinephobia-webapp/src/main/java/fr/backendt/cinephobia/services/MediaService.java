package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.tmdb.SearchResults;
import fr.backendt.cinephobia.repositories.MediaRepository;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;

@Service
public class MediaService {

    private final MediaRepository repository;

    public MediaService(MediaRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Media> getMovie(Long id) {
        return repository.getMovie(id)
                .map(CompletableFuture::completedFuture)
                .orElse(failedFuture(new EntityNotFoundException("Movie not found")));
    }

    @Async
    public CompletableFuture<Media> getSeries(Long id) {
        return repository.getSeries(id)
                .map(CompletableFuture::completedFuture)
                .orElse(failedFuture(new EntityNotFoundException("Series not found")));
    }

    @Async
    public CompletableFuture<SearchResults> getMedias(@Nullable String searchString, int page) {
        if(searchString != null && searchString.isBlank()) searchString = null;
        SearchResults medias = Optional.ofNullable(searchString)
                .map(search -> repository.searchMedias(search, page))
                .orElseGet(() -> repository.getPopularMovies(page));
        return completedFuture(medias);
    }

}
