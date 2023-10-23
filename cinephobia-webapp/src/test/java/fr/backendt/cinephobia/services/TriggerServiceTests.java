package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.repositories.TriggerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class TriggerServiceTests {

    private TriggerRepository repository;
    private TriggerService service;

    @BeforeEach
    void initTests() {
        repository = Mockito.mock(TriggerRepository.class);
        service = new TriggerService(repository);
    }

    @Test
    void createTriggerTest() throws EntityException {
        // GIVEN
        Trigger trigger = new Trigger(1L, "Testphobia", "Fear of tests");
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
        Trigger trigger = new Trigger(1L, "Testphobia", "Fear of tests");

        when(repository.existsByNameIgnoreCase(any()))
                .thenReturn(true);
        // THEN
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createTrigger(trigger).join())
                .withCauseExactlyInstanceOf(EntityException.class);

        verify(repository).existsByNameIgnoreCase(trigger.getName());
        verify(repository, never()).save(any());
    }

    @Test
    void getAllTriggersTest() {
        // GIVEN
        Trigger trigger = new Trigger(1L, "Testphobia", "Fear of tests");
        List<Trigger> triggers = List.of(trigger);
        List<Trigger> results;

        when(repository.findAll()).thenReturn(triggers);
        // WHEN
        results = service.getAllTriggers().join();

        // THEN
        verify(repository).findAll();
        assertThat(results).containsExactly(trigger);
    }

    @Test
    void getTriggersContainingNameTest() {
        // GIVEN
        Trigger trigger = new Trigger(1L, "Testphobia", "Fear of tests");
        String searchString = "test";
        List<Trigger> triggers = List.of(trigger);
        List<Trigger> results;

        when(repository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(any(), any()))
                .thenReturn(triggers);
        // WHEN
        results = service.getTriggersContainingString(searchString).join();

        // THEN
        verify(repository).findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchString, searchString);
        assertThat(results).containsExactly(trigger);
    }

    @Test
    void getTriggerByIdTest() throws EntityNotFoundException {
        // GIVEN
        Long triggerId = 1L;
        Trigger trigger = new Trigger(triggerId, "Testphobia", "Fear of tests");
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

}
