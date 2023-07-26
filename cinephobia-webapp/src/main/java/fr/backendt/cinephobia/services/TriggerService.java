package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.ModelException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.repositories.TriggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static fr.backendt.cinephobia.exceptions.ModelException.ModelNotFoundException;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Service
public class TriggerService {

    private final TriggerRepository repository;

    @Autowired
    public TriggerService(TriggerRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Trigger> createTrigger(Trigger trigger) throws ModelException {
        trigger.setId(null);
        try {
            Trigger savedTrigger = repository.save(trigger);
            return completedFuture(savedTrigger);
        } catch(DataIntegrityViolationException exception) {
            throw new ModelException("Trigger already exists");
        }
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
    public CompletableFuture<Trigger> getTrigger(Long id) throws ModelNotFoundException {
        Trigger trigger = repository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Trigger not found"));
        return completedFuture(trigger);
    }

}
