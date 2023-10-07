package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.repositories.PlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlatformServiceTests {

    private PlatformRepository repository;
    private PlatformService service;

    @BeforeEach
    void initTests() {
        repository = Mockito.mock(PlatformRepository.class);
        service = new PlatformService(repository);
    }

    @Test
    void createPlatformTest() throws EntityException {
        // GIVEN
        Platform platform = new Platform(1L, "JUnit TV");
        Platform expected = new Platform(platform);
        expected.setId(null);
        Platform result;

        when(repository.existsByNameIgnoreCase(any()))
                .thenReturn(false);
        when(repository.save(any())).thenReturn(platform);
        // WHEN
        result = service.createPlatform(platform).join();

        // THEN
        verify(repository).existsByNameIgnoreCase(platform.getName());
        verify(repository).save(expected);
        assertThat(result).isEqualTo(platform);
    }

    @Test
    void failToCreateDuplicatePlatformTest() {
        // GIVEN
        Platform duplicatePlatform = new Platform("Cinema");
        when(repository.existsByNameIgnoreCase(any()))
                .thenReturn(true);
        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createPlatform(duplicatePlatform).join())
                .withCauseExactlyInstanceOf(EntityException.class);

        verify(repository).existsByNameIgnoreCase(duplicatePlatform.getName());
        verify(repository, never()).save(any());
    }

    @Test
    void getAllPlatformsTest() {
        // GIVEN
        List<Platform> platforms = List.of(new Platform("Example"));
        List<Platform> results;

        when(repository.findAll()).thenReturn(platforms);
        // WHEN
        results = service.getAllPlatforms().join();

        // THEN
        verify(repository).findAll();
        assertThat(results).containsExactlyElementsOf(platforms);
    }

    @Test
    void getPlatformsContainingNameTest() {
        // GIVEN
        List<Platform> platforms = List.of(new Platform("Example"));
        List<Platform> results;
        String nameSearch = "ample";

        when(repository.findAllByNameContainingIgnoreCase(any()))
                .thenReturn(platforms);
        // WHEN
        results = service.getPlatformsContainingInName(nameSearch).join();

        // THEN
        verify(repository).findAllByNameContainingIgnoreCase(nameSearch);
        assertThat(results).containsExactlyElementsOf(platforms);
    }

    @Test
    void getPlatformByIdTest() throws EntityNotFoundException {
        // GIVEN
        Long platformId = 1L;
        Platform platform = new Platform(platformId, "Hello");
        Platform result;

        when(repository.findById(any()))
                .thenReturn(Optional.of(platform));
        // WHEN
        result = service.getPlatform(platformId).join();

        // THEN
        verify(repository).findById(platformId);
        assertThat(result).isEqualTo(platform);
    }

}
