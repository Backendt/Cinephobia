package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.repositories.MediaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

@Service
public class MediaService {

    private final MediaRepository repository;

    public MediaService(MediaRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Media> createMedia(Media media) throws EntityException {
        boolean mediaAlreadyExists = repository.existsByTitleIgnoreCase(media.getTitle());
        if(mediaAlreadyExists) {
            return failedFuture(
                    new EntityException("Media already exists")
            );
        }
        media.setId(null);
        Media savedMedia = repository.save(media);
        return completedFuture(savedMedia);
    }

    @Async
    public CompletableFuture<Media> getMedia(Long id) throws EntityNotFoundException {
        return repository.findById(id)
                .map(CompletableFuture::completedFuture)
                .orElse(failedFuture(
                        new EntityNotFoundException("Media not found")
                ));
    }

    @Async
    public CompletableFuture<Page<Media>> getMediaPage(@Nullable String search, Pageable pageable) {
        Page<Media> page = search == null ?
                repository.findAll(pageable) :
                repository.findAllByTitleContainingIgnoreCase(search, pageable);
        return completedFuture(page);
    }

}
