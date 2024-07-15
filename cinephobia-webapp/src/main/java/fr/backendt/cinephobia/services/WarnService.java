package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.repositories.WarnRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
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
    public CompletableFuture<Warn> createWarn(Warn warn) {
        Long userId = warn.getUser().getId();
        Long triggerId = warn.getTrigger().getId();
        boolean alreadyExists = repository.existsByUserIdAndTriggerIdAndMediaIdAndMediaType(userId, triggerId, warn.getMediaId(), warn.getMediaType());

        if(alreadyExists) {
            return failedFuture(new BadRequestException("Warn already exists"));
        }

        warn.setId(null);
        Warn savedWarn = repository.save(warn);
        return completedFuture(savedWarn);
    }

    @Async
    public CompletableFuture<Page<Warn>> getWarnsForMedia(Long mediaId, MediaType type, Pageable pageable) {
        Page<Warn> warns = repository.findAllByMediaIdAndMediaType(mediaId, type, pageable);
        return completedFuture(warns);
    }

    @Async
    public CompletableFuture<Page<Warn>> getWarnsForUser(String userEmail, Pageable pageable) {
        Page<Warn> warns = repository.findAllByUserEmail(userEmail, pageable);
        return completedFuture(warns);
    }

    @Async
    public CompletableFuture<Warn> getWarn(Long warnId) {
        return repository.findById(warnId)
                .map(CompletableFuture::completedFuture)
                .orElse(failedFuture(new EntityNotFoundException("Warn does not exist")));
    }

    @Async
    public CompletableFuture<Warn> updateWarn(Long warnId, Warn update) {
        Optional<Warn> optionalWarn = repository.findById(warnId);
        if(optionalWarn.isEmpty()) {
            return failedFuture(new EntityNotFoundException("Warn does not exist"));
        }
        Warn currentWarn = optionalWarn.get();

        update.setId(null);
        update.setUser(null);

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true);

        boolean isDuplicate = isUpdatingToDuplicate(currentWarn, update);
        if(isDuplicate) {
            return failedFuture(new BadRequestException("Warn already exists"));
        }
        mapper.map(update, currentWarn);
        Warn savedWarn = repository.save(currentWarn);
        return completedFuture(savedWarn);
    }

    @Async
    public CompletableFuture<Warn> updateWarnIfOwnedByUser(Long warnId, Warn update, String ownerEmail) {
        boolean warnExistsAndOwned = repository.existsByIdAndUserEmail(warnId, ownerEmail);
        if(!warnExistsAndOwned) {
            return failedFuture(new EntityNotFoundException("Warn does not exist"));
        }

        return updateWarn(warnId, update);
    }

    @Async
    public CompletableFuture<Void> deleteWarn(Long warnId) {
        boolean warnExists = repository.existsById(warnId);
        if(!warnExists) {
            return failedFuture(new EntityNotFoundException("Warn does not exist"));
        }
        repository.deleteById(warnId);
        return completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> deleteWarnIfOwnedByUser(Long warnId, String ownerEmail) {
        boolean warnExistsAndOwned = repository.existsByIdAndUserEmail(warnId, ownerEmail);
        if(!warnExistsAndOwned) {
            return failedFuture(new EntityNotFoundException("Warn does not exist"));
        }
        repository.deleteById(warnId);
        return completedFuture(null);
    }

    public boolean isUpdatingToDuplicate(Warn current, Warn update) {
        boolean isChangingUniqueFields = isChangingUniqueFields(current, update);
        if(!isChangingUniqueFields) {
            return false;
        }

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true);

        Warn warn = new Warn(current);
        mapper.map(update, warn);

       return repository.existsByUserIdAndTriggerIdAndMediaIdAndMediaType(
               warn.getUser().getId(),
               warn.getTrigger().getId(),
               warn.getMediaId(),
               warn.getMediaType());
    }

    public boolean isChangingUniqueFields(Warn current, Warn update) {
        Trigger updateTrigger = update.getTrigger();
        Long newTriggerId = updateTrigger == null ? null : updateTrigger.getId();
        Long newMediaId = update.getMediaId();
        MediaType newMediaType = update.getMediaType();

        return (newTriggerId != null && !newTriggerId.equals(current.getTrigger().getId())) ||
                (newMediaId != null && !newMediaId.equals(current.getMediaId())) ||
                (newMediaType != null && !newMediaType.equals(current.getMediaType()));
    }

}
