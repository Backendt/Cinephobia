package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.repositories.TriggerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class TriggerServiceTests {

    private TriggerRepository repository;
    private TriggerService service;

    private Trigger trigger;

    @BeforeEach
    void initTests() {
        repository = Mockito.mock(TriggerRepository.class);
        service = new TriggerService(repository);

        trigger = new Trigger(1L, "Testphobia", "Fear of tests");
    }

    @Test
    void createTriggerTest() throws BadRequestException {
        // GIVEN
        Trigger expected = new Trigger(trigger);
        expected.setId(null);
        Trigger result;

        when(repository.existsByNameIgnoreCase(any()))
                .thenReturn(false);
        when(repository.save(any())).thenReturn(trigger);
        // WHEN
        result = service.createTrigger(trigger).join();

        // THEN
        assertThat(result).isEqualTo(trigger);
        verify(repository).existsByNameIgnoreCase(trigger.getName());
        verify(repository).save(expected);
    }

    @Test
    void failToCreateDuplicateTriggerTest() {
        // GIVEN
        when(repository.existsByNameIgnoreCase(any()))
                .thenReturn(true);
        // THEN
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createTrigger(trigger).join())
                .withCauseExactlyInstanceOf(BadRequestException.class);

        verify(repository).existsByNameIgnoreCase(trigger.getName());
        verify(repository, never()).save(any());
    }

    @Test
    void getTriggersTest() {
        // GIVEN
        List<Trigger> triggers = List.of(trigger);

        Pageable pageable = PageRequest.of(0, 5);
        Page<Trigger> triggerPage = new PageImpl<>(triggers);

        Page<Trigger> results;

        when(repository.findAll(any(Pageable.class))).thenReturn(triggerPage);
        // WHEN
        results = service.getTriggers(null, pageable).join();

        // THEN
        verify(repository).findAll(pageable);
        verify(repository, never()).findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(any(), any(), any());
        assertThat(results).isEqualTo(triggerPage);
        assertThat(results.getContent()).containsExactlyElementsOf(triggers);
    }

    @Test
    void getTriggersWithSearchTest() {
        // GIVEN
        String search = "test";
        List<Trigger> triggers = List.of(trigger);

        Pageable pageable = PageRequest.of(0, 5);
        Page<Trigger> triggersPage = new PageImpl<>(triggers);

        Page<Trigger> results;

        when(repository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(any(), any(), any()))
                .thenReturn(triggersPage);
        // WHEN
        results = service.getTriggers(search, pageable).join();

        // THEN
        verify(repository).findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        verify(repository, never()).findAll(any(Pageable.class));
        assertThat(results).isEqualTo(triggersPage);
        assertThat(results.getContent()).containsExactlyElementsOf(triggers);
    }

    @Test
    void getTriggerByIdTest() throws EntityNotFoundException {
        // GIVEN
        Long triggerId = 1L;
        Trigger result;

        when(repository.findById(any())).thenReturn(Optional.of(trigger));
        // WHEN
        result = service.getTrigger(triggerId).join();

        // THEN
        verify(repository).findById(triggerId);
        assertThat(result).isEqualTo(trigger);
    }

    @Test
    void getUnknownTriggerByIdTest() {
        // GIVEN
        Long triggerId = 1L;

        when(repository.findById(any())).thenReturn(Optional.empty());
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getTrigger(triggerId).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        // THEN
        verify(repository).findById(triggerId);
    }

    @Test
    void updateTriggerTest() {
        // GIVEN
        long triggerId = 1L;
        String newName = "New trigger";

        Trigger triggerUpdate = new Trigger();
        triggerUpdate.setName(newName);

        Trigger expectedTrigger = new Trigger(trigger);
        expectedTrigger.setName(newName);

        Trigger result;

        when(repository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(repository.findById(any())).thenReturn(Optional.of(trigger));
        when(repository.save(any())).thenReturn(expectedTrigger);
        // WHEN
        result = service.updateTrigger(triggerId, triggerUpdate).join();

        // THEN
        verify(repository).existsByNameIgnoreCase(newName);
        verify(repository).findById(triggerId);
        verify(repository).save(expectedTrigger);
        assertThat(result).isEqualTo(expectedTrigger);
    }

    @Test
    void updateToDuplicateTriggerTest() {
        // GIVEN
        long triggerId = 1L;
        String newName = "New trigger";

        Trigger triggerUpdate = new Trigger();
        triggerUpdate.setName(newName);

        when(repository.findById(any())).thenReturn(Optional.of(trigger));
        when(repository.existsByNameIgnoreCase(any())).thenReturn(true);
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateTrigger(triggerId, triggerUpdate).join())
                .withCauseExactlyInstanceOf(BadRequestException.class);

        // THEN
        verify(repository).findById(triggerId);
        verify(repository).existsByNameIgnoreCase(newName);
        verify(repository, never()).save(any());
    }

    @Test
    void updateUnknownTriggerTest() {
        // GIVEN
        long triggerId = 1L;
        String newName = "New trigger";

        Trigger triggerUpdate = new Trigger();
        triggerUpdate.setName(newName);

        when(repository.findById(any())).thenReturn(Optional.empty());
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateTrigger(triggerId, triggerUpdate).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        // THEN
        verify(repository).findById(triggerId);
        verify(repository, never()).existsByNameIgnoreCase(any());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteTriggerTest() {
        // GIVEN
        long triggerId = 1L;

        when(repository.existsById(any())).thenReturn(true);
        // WHEN
        service.deleteTrigger(triggerId).join();

        // THEN
        verify(repository).existsById(triggerId);
        verify(repository).deleteById(triggerId);
    }

    @Test
    void deleteUnknownTriggerTest() {
        // GIVEN
        long triggerId = 1L;

        when(repository.existsById(any())).thenReturn(false);
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.deleteTrigger(triggerId).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        // THEN
        verify(repository).existsById(triggerId);
        verify(repository, never()).deleteById(any());
    }

}
