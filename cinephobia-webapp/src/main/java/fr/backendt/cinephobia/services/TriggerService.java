package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.repositories.TriggerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

@Service
public class TriggerService {

    private final TriggerRepository repository;

    public TriggerService(TriggerRepository repository) {
        this.repository = repository;
    }

    @Async
    public CompletableFuture<Trigger> createTrigger(Trigger trigger) throws BadRequestException {
        boolean triggerAlreadyExists = repository.existsByNameIgnoreCase(trigger.getName());
        if(triggerAlreadyExists) {
            return failedFuture(
                    new BadRequestException("Trigger already exists")
            );
        }
        trigger.setId(null);
        Trigger savedTrigger = repository.save(trigger);
        return completedFuture(savedTrigger);
    }

    @Async
    public CompletableFuture<Page<Trigger>> getTriggers(@Nullable String search, Pageable pageable) {
        Page<Trigger> triggers = search != null ?
                repository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable) :
                repository.findAll(pageable);
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

    @Async
    public CompletableFuture<Trigger> updateTrigger(Long id, Trigger triggerUpdate) throws EntityNotFoundException {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true);

        triggerUpdate.setId(null);
        CompletableFuture<Trigger> currentTrigger = repository.findById(id)
                .map(CompletableFuture::completedFuture)
                .orElse(failedFuture(new EntityNotFoundException("Trigger not found")));

        return currentTrigger.thenCompose(trigger -> {
            boolean isChangingTriggerName = triggerUpdate.getName() != null && !trigger.getName().equals(triggerUpdate.getName());
            if(isChangingTriggerName) {
                String newName = triggerUpdate.getName();
                boolean triggerAlreadyExists = repository.existsByNameIgnoreCase(newName);
                if(triggerAlreadyExists) {
                    return failedFuture(new BadRequestException("Trigger already exists"));
                }
            }

            mapper.map(triggerUpdate, trigger);
            Trigger savedTrigger = repository.save(trigger);
            return completedFuture(savedTrigger);
        });
    }

    @Async
    public CompletableFuture<Void> deleteTrigger(Long id) throws EntityNotFoundException {
        boolean triggerExists = repository.existsById(id);
        if(!triggerExists) {
            return failedFuture(new EntityNotFoundException("Trigger not found"));
        }
        repository.deleteById(id);
        return completedFuture(null);
    }

}
