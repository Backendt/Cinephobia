package fr.backendt.cinephobia.mappers;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.models.dto.MediaDTO;
import fr.backendt.cinephobia.services.PlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MediaMapperTests {

    private PlatformService service;
    private MediaMapper mapper;


    private Platform platformTest;
    private Media mediaTest;
    private MediaDTO dtoTest;

    @BeforeEach
    void initTest() {
        service = Mockito.mock(PlatformService.class);
        mapper = new MediaMapper(service);

        platformTest = new Platform(1L, "Example platform");
        mediaTest = new Media("Media test", "Description test", List.of(platformTest));
        dtoTest = new MediaDTO(null, "Media test", "Description test", List.of(1L));
    }

    @Test
    void convertEntityToDtoTest() {
        // GIVEN
        MediaDTO result;

        // WHEN
        result = mapper.toDTO(mediaTest);

        // THEN
        assertThat(result).isEqualTo(dtoTest);
    }

    @Test
    void convertDtoToEntityTest() throws EntityException.EntityNotFoundException {
        // GIVEN
        Media result;

        when(service.getPlatform(any()))
                .thenReturn(CompletableFuture.completedFuture(platformTest));
        // WHEN
        result = mapper.toEntity(dtoTest);

        // THEN
        verify(service).getPlatform(1L);
        assertThat(result).isEqualTo(mediaTest);
    }

    @Test
    void convertDtoWithUnknownPlatformToEntityTest() throws EntityException.EntityNotFoundException {
        // GIVEN
        when(service.getPlatform(any()))
                .thenThrow(EntityException.EntityNotFoundException.class);
        // WHEN
        assertThatExceptionOfType(EntityException.EntityNotFoundException.class)
                .isThrownBy(() -> mapper.toEntity(dtoTest));

        // THEN
        verify(service).getPlatform(1L);
    }

}
