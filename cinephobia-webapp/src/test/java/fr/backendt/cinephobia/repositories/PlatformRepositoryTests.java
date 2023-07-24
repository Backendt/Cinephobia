package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Platform;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static fr.backendt.cinephobia.TestingModelValues.PLATFORM_TEST;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class PlatformRepositoryTests {

    @Autowired
    private PlatformRepository repository;

    @Test
    void createPlatformTest() {
        // GIVEN
        Platform result;

        // WHEN
        result = repository.save(PLATFORM_TEST);

        // THEN
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void getAllPlatformsTest() {
        // GIVEN
        List<Platform> resultsBefore;
        List<Platform> resultsAfter;

        // WHEN
        resultsBefore = repository.findAll();
        repository.save(PLATFORM_TEST);
        resultsAfter = repository.findAll();

        // THEN
        assertThat(resultsBefore).isEmpty();
        assertThat(resultsAfter).isNotEmpty();
    }

    @Test
    void getPlatformByIdTest() {
        // GIVEN
        Platform expected;
        Optional<Platform> result;

        // WHEN
        expected = repository.save(PLATFORM_TEST);
        result = repository.findById(expected.getId());

        // THEN
        assertThat(result).contains(expected);
    }

    @Test
    void getPlatformByNameTest() {
        // GIVEN
        Platform expected;
        Optional<Platform> result;

        // WHEN
        expected = repository.save(PLATFORM_TEST);
        result = repository.findByName(PLATFORM_TEST.getName());

        // THEN
        assertThat(result).contains(expected);
    }

    @Test
    void getPlatformsContainingNameTest() {
        // GIVEN
        Platform expected;
        List<Platform> results;
        String namePart = PLATFORM_TEST.getName()
                .toUpperCase()
                .substring(2, 6);

        // WHEN
        expected = repository.save(PLATFORM_TEST);
        results = repository.findAllByNameContainingIgnoreCase(namePart);

        // THEN
        assertThat(results).containsExactly(expected);
    }

    @Test
    void failToGetPlatformsContainingNameTest() {
        // GIVEN
        List<Platform> results;
        String nonexistentNamePart = "hey";

        // WHEN
        repository.save(PLATFORM_TEST);
        results = repository.findAllByNameContainingIgnoreCase(nonexistentNamePart);

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void deletePlatformTest() {
        // GIVEN
        Platform savedPlatform;
        Optional<Platform> resultBefore;
        Optional<Platform> resultAfter;

        // WHEN
        savedPlatform = repository.save(PLATFORM_TEST);
        resultBefore = repository.findById(savedPlatform.getId());
        repository.deleteById(savedPlatform.getId());
        resultAfter = repository.findById(savedPlatform.getId());

        // THEN
        assertThat(resultBefore).isNotEmpty();
        assertThat(resultAfter).isEmpty();
    }

}
