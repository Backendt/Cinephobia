package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.ModelException;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.repositories.WarnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static fr.backendt.cinephobia.exceptions.ModelException.ModelNotFoundException;
import static java.util.concurrent.CompletableFuture.completedFuture;

@Service
public class WarnService {

    private final WarnRepository repository;

    @Autowired
    public WarnService(WarnRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Warn> createWarn(Warn warn) throws ModelException {
        warn.setId(null);
        try {
            Warn savedWarn = repository.save(warn);
            return completedFuture(savedWarn);
        } catch(DataIntegrityViolationException | InvalidDataAccessApiUsageException exception) {
            throw new ModelException("Unknown media and/or trigger");
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
    public CompletableFuture<Warn> getWarn(Long id) throws ModelNotFoundException {
        Warn warn = repository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Warn not found"));
        return completedFuture(warn);
    }
}
