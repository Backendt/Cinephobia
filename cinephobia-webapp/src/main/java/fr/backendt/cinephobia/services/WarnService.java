package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.repositories.WarnRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

@Service
public class WarnService {

    private final WarnRepository repository;

    public WarnService(WarnRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Warn> createWarn(Warn warn) throws BadRequestException {
        warn.setId(null);
        try {
            Warn savedWarn = repository.save(warn);
            return completedFuture(savedWarn);
        } catch(DataIntegrityViolationException | InvalidDataAccessApiUsageException exception) {
            return failedFuture(
                    new BadRequestException("Unknown media and/or trigger")
            );
        }
    }

    @Async
    public CompletableFuture<Page<Warn>> getAllWarns(Pageable pageable) {
        Page<Warn> warns = repository.findAll(pageable);
        return completedFuture(warns);
    }

    @Async
    public CompletableFuture<Page<Warn>> getWarnsByMedia(Long mediaId, MediaType type, Pageable pageable) {
        Page<Warn> warns = repository.findAllByMediaIdAndMediaType(mediaId, type, pageable);
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
