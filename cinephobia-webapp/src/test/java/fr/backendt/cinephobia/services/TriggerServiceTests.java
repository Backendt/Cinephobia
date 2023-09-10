package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.repositories.TriggerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TriggerServiceTests {

    private TriggerRepository repository;
    private TriggerService service;

    @BeforeEach
    void initTests() {
        repository = Mockito.mock(TriggerRepository.class);
        service = new TriggerService(repository);
    }

    @Test
    void createTriggerTest() {
        // GIVEN
        Trigger trigger = new Trigger(1L, "Testphobia", "Fear of tests");
        Trigger expected = new Trigger(trigger);
        expected.setId(null);
        Trigger result;

        when(repository.save(any())).thenReturn(trigger);
        // WHEN
        result = service.createTrigger(trigger).join();

        // THEN
        assertThat(result).isEqualTo(trigger);
        verify(repository).save(expected);
    }

    @Test
    void failToCreateDuplicateTriggerTest() {
        // GIVEN
        Trigger trigger = new Trigger(1L, "Testphobia", "Fear of tests");

        when(repository.save(any()))
                .thenThrow(DataIntegrityViolationException.class);
        // THEN
        // WHEN
        assertThatExceptionOfType(EntityException.class)
                .isThrownBy(() -> service.createTrigger(trigger));
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
    void getTriggerByIdTest() {
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
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> service.getTrigger(triggerId));

        // THEN
        verify(repository).findById(triggerId);
    }

}
