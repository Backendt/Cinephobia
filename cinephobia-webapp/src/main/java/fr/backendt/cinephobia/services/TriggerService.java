package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.repositories.TriggerRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

@Service
public class TriggerService {

    private final TriggerRepository repository;

    public TriggerService(TriggerRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Trigger> createTrigger(Trigger trigger) throws EntityException {
        boolean triggerAlreadyExists = repository.existsByNameIgnoreCase(trigger.getName());
        if(triggerAlreadyExists) {
            return failedFuture(
                    new EntityException("Trigger already exists")
            );
        }
        trigger.setId(null);
        Trigger savedTrigger = repository.save(trigger);
        return completedFuture(savedTrigger);
    }

    @Async
    public CompletableFuture<List<Trigger>> getAllTriggers() {
        List<Trigger> triggers = repository.findAll();
        return completedFuture(triggers);
    }

    @Async
    public CompletableFuture<List<Trigger>> getTriggersContainingString(String search) {
        List<Trigger> triggers = repository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        return completedFuture(triggers);
    }

    @Async
    public CompletableFuture<Trigger> getTrigger(Long id) throws EntityNotFoundException {
        return repository.findById(id)
                .map(CompletableFuture::completedFuture)
                .orElse(failedFuture(
                        new EntityNotFoundException("Trigger not found")
                ));
    }

}
