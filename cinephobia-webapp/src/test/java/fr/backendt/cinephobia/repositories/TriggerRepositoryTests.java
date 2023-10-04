package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Trigger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TriggerRepositoryTests {

    @Autowired
    private TriggerRepository repository;

    @Test
    void createTriggerTest() {
        // GIVEN
        Trigger trigger = new Trigger("Technophobia", "Fear of technology");
        Trigger result;

        // WHEN
        result = repository.save(trigger);

        // THEN
        assertThat(result).hasNoNullFieldsOrProperties();
    }

    @Test
    void getTriggerByIdTest() {
        // GIVEN
        Long triggerId = 1L;
        Optional<Trigger> result;

        // WHEN
        result = repository.findById(triggerId);

        // THEN
        assertThat(result).isNotEmpty();
        assertThat(result.get()).hasNoNullFieldsOrProperties();
    }

    @Test
    void getTriggersContainingNameTest() {
        // GIVEN
        String fullName = "Testphobia";
        String namePart = "estp";
        List<Trigger> results;

        // WHEN
        results = repository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(namePart, namePart);

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).isEqualTo(fullName);
    }

    @Test
    void getTriggersContainingDescriptionTest() {
        // GIVEN
        String fullDescription = "Fear of software bugs";
        String descriptionPart = "Soft";
        List<Trigger> results;

        // WHEN
        results = repository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(descriptionPart, descriptionPart);

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getDescription()).isEqualTo(fullDescription);
    }
    @Test
    void failToGetTriggersContainingNameOrDescriptionTest() {
        // GIVEN
        List<Trigger> results;
        String incorrectSearch = "techno";

        // WHEN
        results = repository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(incorrectSearch, incorrectSearch);

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void deleteTriggerByIdTest() {
        // GIVEN
        Long triggerId = 1L;
        Optional<Trigger> resultBefore;
        Optional<Trigger> resultAfter;

        // WHEN
        resultBefore = repository.findById(triggerId);
        repository.deleteById(triggerId);
        resultAfter = repository.findById(triggerId);

        // THEN
        assertThat(resultBefore).isNotEmpty();
        assertThat(resultAfter).isEmpty();
    }


}
