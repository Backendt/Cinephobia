package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.repositories.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;
import static java.util.concurrent.CompletableFuture.completedFuture;

@Service
public class MediaService {

    private final MediaRepository repository;

    @Autowired
    public MediaService(MediaRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Media> createMedia(Media media) throws EntityException {
        media.setId(null);
        try {
            Media savedMedia = repository.save(media);
            return completedFuture(savedMedia);
        } catch(DataIntegrityViolationException exception) {
            throw new EntityException("Media already exists");
        }
    }

    @Async
    public CompletableFuture<List<Media>> getAllMedias() {
        List<Media> medias = repository.findAll();
        return completedFuture(medias);
    }

    @Async
    public CompletableFuture<Media> getMedia(Long id) throws EntityNotFoundException {
        Media media = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media not found"));
        return completedFuture(media);
    }

    @Async
    public CompletableFuture<List<Media>> getMediaContainingInTitle(String search) {
        List<Media> medias = repository.findAllByTitleContainingIgnoreCase(search);
        return completedFuture(medias);
    }

}
