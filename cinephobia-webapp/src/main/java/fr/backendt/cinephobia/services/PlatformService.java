package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.repositories.PlatformRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

@Service
public class PlatformService {

    private final PlatformRepository repository;

    public PlatformService(PlatformRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Platform> createPlatform(Platform platform) throws EntityException {
        boolean platformAlreadyExists = repository.existsByNameIgnoreCase(platform.getName());
        if(platformAlreadyExists) {
            return failedFuture(
                    new EntityException("Platform already exists")
            );
        }
        platform.setId(null);
        Platform savedPlatform = repository.save(platform);
        return completedFuture(savedPlatform);
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
    public CompletableFuture<Platform> getPlatform(Long id) throws EntityNotFoundException {
        return repository.findById(id)
                .map(CompletableFuture::completedFuture)
                .orElse(failedFuture(
                        new EntityNotFoundException("Platform not found")
                ));
    }

}
