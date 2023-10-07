package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlatformRepositoryTests {

    @Autowired
    private PlatformRepository repository;

    @Test
    void createPlatformTest() {
        // GIVEN
        Platform platform = new Platform("JUnit TV");
        Platform result;

        // WHEN
        result = repository.save(platform);

        // THEN
        assertThat(result).hasNoNullFieldsOrProperties();
    }

    @Test
    void getAllPlatformsTest() {
        // GIVEN
        List<Platform> results;

        // WHEN
        results = repository.findAll();

        // THEN
        assertThat(results).isNotEmpty();
    }

    @Test
    void getPlatformByIdTest() {
        // GIVEN
        Long platformId = 1L;
        Optional<Platform> result;

        // WHEN
        result = repository.findById(platformId);

        // THEN
        assertThat(result).isNotEmpty();
        assertThat(result.get()).hasNoNullFieldsOrProperties();
    }

    @Test
    void getPlatformsContainingNameTest() {
        // GIVEN
        String partTitle = "cine";
        String fullTitle = "Cinema";
        List<Platform> results;

        // WHEN
        results = repository.findAllByNameContainingIgnoreCase(partTitle);

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).isEqualTo(fullTitle);
    }

    @Test
    void failToGetPlatformsContainingNameTest() {
        // GIVEN
        List<Platform> results;
        String nonexistentNamePart = "hey";

        // WHEN
        results = repository.findAllByNameContainingIgnoreCase(nonexistentNamePart);

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void deletePlatformTest() {
        // GIVEN
        Long platformId = 1L;
        Optional<Platform> resultBefore;
        Optional<Platform> resultAfter;

        // WHEN
        resultBefore = repository.findById(platformId);
        repository.deleteById(platformId);
        resultAfter = repository.findById(platformId);

        // THEN
        assertThat(resultBefore).isNotEmpty();
        assertThat(resultAfter).isEmpty();
    }

    @CsvSource({
            "Prime Video",
            "prime video",
            "Disney+",
            "disney+"
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
            "Disney +",
            "netfilx",
            "prime-video"
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
