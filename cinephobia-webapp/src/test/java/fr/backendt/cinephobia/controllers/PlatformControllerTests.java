package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.services.PlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformControllerTests {

    private PlatformController controller;
    private PlatformService service;

    private Platform platformTest;

    @BeforeEach
    void initTests() {
        service = Mockito.mock(PlatformService.class);
        controller = new PlatformController(service);

        platformTest = new Platform("Junit TV");
    }

    @Test
    void getPlatformsTest() {
        // GIVEN
        String searchString = null;
        List<Platform> platforms = List.of(platformTest);
        List<Platform> results;

        when(service.getAllPlatforms())
                .thenReturn(CompletableFuture.completedFuture(platforms));
        // WHEN
        results = controller.getPlatforms(searchString).join();

        // THEN
        verify(service).getAllPlatforms();
        assertThat(results).containsExactlyElementsOf(platforms);
    }

    @Test
    void searchPlatformsTest() {
        // GIVEN
        String searchString = "uni";
        List<Platform> platforms = List.of(platformTest);
        List<Platform> results;

        when(service.getPlatformsContainingInName(any()))
                .thenReturn(CompletableFuture.completedFuture(platforms));
        // WHEN
        results = controller.getPlatforms(searchString).join();

        // THEN
        verify(service).getPlatformsContainingInName(searchString);
        assertThat(results).containsExactlyElementsOf(platforms);
    }

    @Test
    void getPlatformTest() {
        // GIVEN
        Long platformId = 1L;
        Platform result;

        when(service.getPlatform(any()))
                .thenReturn(CompletableFuture.completedFuture(platformTest));
        // WHEN
        result = controller.getPlatform(platformId).join();

        // THEN
        verify(service).getPlatform(platformId);
        assertThat(result).isEqualTo(platformTest);
    }

    @Test
    void createPlatformTest() {
        // GIVEN
        Platform savedPlatform = new Platform(platformTest);
        savedPlatform.setId(1L);

        Platform result;

        when(service.createPlatform(any()))
                .thenReturn(CompletableFuture.completedFuture(savedPlatform));
        // WHEN
        result = controller.createPlatform(platformTest).join();

        // THEN
        verify(service).createPlatform(platformTest);
        assertThat(result).isEqualTo(savedPlatform);
    }

}
