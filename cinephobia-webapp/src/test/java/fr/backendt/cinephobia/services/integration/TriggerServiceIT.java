package fr.backendt.cinephobia.services.integration;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.services.TriggerService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletionException;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Transactional
@SpringBootTest
class TriggerServiceIT {

    @Autowired
    private TriggerService service;

    @Test
    void createTriggerTest() {
        // GIVEN
        Trigger trigger = new Trigger("Vulnphobia", "Fear of introducing a vulnerability in a software");
        Trigger result;

        // WHEN
        result = service.createTrigger(trigger).join();

        // THEN
        assertThat(result).hasNoNullFieldsOrProperties();
        assertThat(result.getName()).isEqualTo(trigger.getName());
        assertThat(result.getDescription()).isEqualTo(trigger.getDescription());
    }

    @Test
    void createDuplicateTriggerTest() {
        // GIVEN
        Trigger duplicateTrigger = new Trigger("Testphobia", "Description does not have to be unique");

        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createTrigger(duplicateTrigger).join())
                .withCauseExactlyInstanceOf(EntityException.class);
    }

    @Test
    void getAllTriggersTest() {
        // GIVEN
        List<Trigger> results;

        // WHEN
        results = service.getAllTriggers().join();

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0)).hasNoNullFieldsOrProperties();
    }

    @Test
    void getTriggersContainingStringTest() {
        // GIVEN
        String nameSearch = "est";
        String expectedName = "Testphobia";
        List<Trigger> results;

        // WHEN
        results = service.getTriggersContainingString(nameSearch).join();

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0)).hasNoNullFieldsOrProperties();
        assertThat(results.get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    void getNoTriggersContainingStringTest() {
        // GIVEN
        String nameSearch = "vuln";
        List<Trigger> results;

        // WHEN
        results = service.getTriggersContainingString(nameSearch).join();

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void getTriggerByIdTest() {
        // GIVEN
        Long triggerId = 1L;
        Trigger result;

        // WHEN
        result = service.getTrigger(triggerId).join();

        // THEN
        assertThat(result).isNotNull()
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void getUnknownTriggerByIdTest() {
        // GIVEN
        Long unknownTriggerId = 1337L;

        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getTrigger(unknownTriggerId).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);
    }




}
