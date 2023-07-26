package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.ModelException;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.repositories.PlatformRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static fr.backendt.cinephobia.exceptions.ModelException.ModelNotFoundException;

@Service
public class PlatformService {

    private final PlatformRepository repository;

    @Autowired
    public PlatformService(PlatformRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Platform> createPlatform(Platform platform) throws ModelException {
        platform.setId(null);
        try {
            Platform savedPlatform = repository.save(platform);
            return completedFuture(savedPlatform);
        } catch(DataIntegrityViolationException exception) {
            throw new ModelException("Platform already exists");
        }
    }

    @Async
    public CompletableFuture<List<Platform>> getAllPlatforms() {
        List<Platform> platforms = repository.findAll();
        return completedFuture(platforms);
    }

    @Async
    public CompletableFuture<List<Platform>> getPlatformsContainingInName(String search) {
        List<Platform> platforms = repository.findAllByNameContainingIgnoreCase(search);
        return completedFuture(platforms);
    }

    @Async
    public CompletableFuture<Platform> getPlatform(Long id) throws ModelNotFoundException {
        Platform platform = repository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Platform not found"));
        return completedFuture(platform);
    }

}
