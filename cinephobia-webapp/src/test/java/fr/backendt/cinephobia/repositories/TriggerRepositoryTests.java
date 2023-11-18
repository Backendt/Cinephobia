package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Trigger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    void searchTriggersNameTest() {
        // GIVEN
        String fullName = "Testphobia";
        String namePart = "estp";
        Pageable pageRequest = PageRequest.of(0, 5);

        int sizeExpected = 1;
        Page<Trigger> results;

        // WHEN
        results = repository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(namePart, namePart, pageRequest);

        // THEN
        assertThat(results.getTotalElements()).isEqualTo(sizeExpected);
        assertThat(results.getContent()).hasSize(sizeExpected);
        assertThat(results.getContent().get(0).getName()).isEqualTo(fullName);
    }

    @Test
    void searchTriggersDescriptionTest() {
        // GIVEN
        String fullDescription = "Fear of software bugs";
        String descriptionPart = "Soft";
        Pageable pageRequest = PageRequest.of(0, 5);

        int sizeExpected = 1;
        Page<Trigger> results;

        // WHEN
        results = repository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(descriptionPart, descriptionPart, pageRequest);

        // THEN
        assertThat(results.getTotalElements()).isEqualTo(sizeExpected);
        assertThat(results.getContent()).hasSize(sizeExpected);
        assertThat(results.getContent().get(0).getDescription()).isEqualTo(fullDescription);
    }
    @Test
    void searchUnknownTriggers() {
        // GIVEN
        Page<Trigger> results;
        String incorrectSearch = "techno";
        Pageable pageRequest = PageRequest.of(0, 5);

        // WHEN
        results = repository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(incorrectSearch, incorrectSearch, pageRequest);

        // THEN
        assertThat(results.getTotalElements()).isZero();
        assertThat(results.getContent()).isEmpty();
    }

    @ValueSource(longs = {1L, 2L})
    @ParameterizedTest
    void deleteTriggerByIdTest(long triggerId) {
        // GIVEN
        boolean existsBefore, existsAfter;

        // WHEN
        existsBefore = repository.existsById(triggerId);
        repository.deleteById(triggerId);
        existsAfter = repository.existsById(triggerId);

        // THEN
        assertThat(existsBefore).isTrue();
        assertThat(existsAfter).isFalse();
    }

    @CsvSource({
            "bugphobia",
            "testphobia",
            "TestPhobia",
            "BUGPHOBIA"
    })
    @ParameterizedTest
    void existsByNameTest(String name) {
        // GIVEN
        boolean result;

        // WHEN
        result = repository.existsByNameIgnoreCase(name);

        // THEN
        assertThat(result).isTrue();
    }

    @CsvSource({
            "bugphobi",
            "test phobia",
            "ugphobia"
    })
    @ParameterizedTest
    void existsByUnknownNameTest(String unknownName) {
        // GIVEN
        boolean result;

        // WHEN
        result = repository.existsByNameIgnoreCase(unknownName);

        // THEN
        assertThat(result).isFalse();
    }

}
