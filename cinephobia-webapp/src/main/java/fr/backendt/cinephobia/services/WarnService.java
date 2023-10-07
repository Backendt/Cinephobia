package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.repositories.WarnRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

@Service
public class WarnService {

    private final WarnRepository repository;

    public WarnService(WarnRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Warn> createWarn(Warn warn) throws EntityException {
        warn.setId(null);
        try {
            Warn savedWarn = repository.save(warn);
            return completedFuture(savedWarn);
        } catch(DataIntegrityViolationException | InvalidDataAccessApiUsageException exception) {
            return failedFuture(
                    new EntityException("Unknown media and/or trigger")
            );
        }
    }

    @Async
    public CompletableFuture<List<Warn>> getAllWarns() {
        List<Warn> warns = repository.findAll();
        return completedFuture(warns);
    }

    @Async
    public CompletableFuture<List<Warn>> getWarnsByMediaId(Long mediaId) {
        List<Warn> warns = repository.findAllByMediaId(mediaId);
        return completedFuture(warns);
    }

    @Async
    public CompletableFuture<Warn> getWarn(Long id) throws EntityNotFoundException {
        return repository.findById(id)
                .map(CompletableFuture::completedFuture)
                .orElse(failedFuture(
                        new EntityNotFoundException("Warn not found")
                ));
    }
}
