package fr.backendt.cinephobia.services.integration;

import fr.backendt.cinephobia.exceptions.ModelException;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.services.PlatformService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletionException;

import static fr.backendt.cinephobia.exceptions.ModelException.ModelNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@Transactional
class PlatformServiceIT {

    @Autowired
    private PlatformService service;

    @Test
    void createPlatformTest() {
        // GIVEN
        Platform unsavedPlatform = new Platform("JUnit TV");
        Platform result;

        // WHEN
        result = service.createPlatform(unsavedPlatform).join();

        // THEN
        assertThat(result).hasNoNullFieldsOrProperties();
    }

    @Test
    void createDuplicatePlatformTest() {
        // GIVEN
        Platform duplicatePlatform = new Platform("Netflix");

        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createPlatform(duplicatePlatform).join())
                .withCauseExactlyInstanceOf(ModelException.class);
    }

    @Test
    void getAllPlatformsTest() {
        // GIVEN
        List<Platform> results;

        // WHEN
        results = service.getAllPlatforms().join();

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0)).hasNoNullFieldsOrProperties();
    }

    @Test
    void getPlatformsContainingInNameTest() {
        // GIVEN
        String nameSearch = "TFLI";
        String expectedName = "Netflix";
        List<Platform> results;

        // WHEN
        results = service.getPlatformsContainingInName(nameSearch).join();

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0)).hasNoNullFieldsOrProperties();
        assertThat(results.get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    void getNoPlatformsContainingInNameTest() {
        // GIVEN
        String nameSearch = "FTLI";
        List<Platform> results;

        // WHEN
        results = service.getPlatformsContainingInName(nameSearch).join();

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void getPlatformByIdTest() {
        // GIVEN
        Long platformId = 1L;
        Platform result;

        // WHEN
        result = service.getPlatform(platformId).join();

        // THEN
        assertThat(result).hasNoNullFieldsOrProperties();
    }

    @Test
    void getUnknownPlatformByIdTest() {
        // GIVEN
        Long unknownPlatformId = 1337L;

        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getPlatform(unknownPlatformId).join())
                .withCauseExactlyInstanceOf(ModelNotFoundException.class);
    }

}

