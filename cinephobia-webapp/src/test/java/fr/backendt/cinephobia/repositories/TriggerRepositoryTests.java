package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Trigger;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static fr.backendt.cinephobia.TestingModelValues.TRIGGER_TEST;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class TriggerRepositoryTests {

    @Autowired
    private TriggerRepository repository;

    @Test
    void createTriggerTest() {
        // GIVEN
        Trigger result;

        // WHEN
        result = repository.save(TRIGGER_TEST);

        // THEN
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void getTriggerByIdTest() {
        // GIVEN
        Trigger expected;
        Optional<Trigger> result;

        // WHEN
        expected = repository.save(TRIGGER_TEST);
        result = repository.findById(expected.getId());

        // THEN
        assertThat(result).contains(expected);
    }

    @Test
    void getTriggerContainingNameTest() {
        // GIVEN
        Trigger expected;
        Optional<Trigger> result;
        String namePart = TRIGGER_TEST.getName()
                .toUpperCase()
                .substring(4);

        // WHEN
        expected = repository.save(TRIGGER_TEST);
        result = repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(namePart, namePart);

        // THEN
        assertThat(result).contains(expected);
    }

    @Test
    void getTriggerContainingDescriptionTest() {
        // GIVEN
        Trigger expected;
        Optional<Trigger> result;
        String descriptionPart = TRIGGER_TEST.getName()
                .toUpperCase()
                .substring(0, 11);

        // WHEN
        expected = repository.save(TRIGGER_TEST);
        result = repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(descriptionPart, descriptionPart);

        // THEN
        assertThat(result).contains(expected);
    }
    @Test
    void failToGetTriggerContainingNameOrDescriptionTest() {
        // GIVEN
        Optional<Trigger> result;
        String incorrectSearch = "tecno";

        // WHEN
        repository.save(TRIGGER_TEST);
        result = repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(incorrectSearch, incorrectSearch);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void deleteTriggerByIdTest() {
        // GIVEN
        Trigger savedTrigger;
        Optional<Trigger> resultBefore;
        Optional<Trigger> resultAfter;

        // WHEN
        savedTrigger = repository.save(TRIGGER_TEST);

        resultBefore = repository.findById(savedTrigger.getId());
        repository.deleteById(savedTrigger.getId());
        resultAfter = repository.findById(savedTrigger.getId());

        // THEN
        assertThat(resultBefore).isNotEmpty();
        assertThat(resultAfter).isEmpty();
    }


}
