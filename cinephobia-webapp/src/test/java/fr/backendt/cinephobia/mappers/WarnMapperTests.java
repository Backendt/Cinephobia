package fr.backendt.cinephobia.mappers;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.models.dto.WarnDTO;
import fr.backendt.cinephobia.services.MediaService;
import fr.backendt.cinephobia.services.TriggerService;
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

class WarnMapperTests {

    private MediaService mediaService;
    private TriggerService triggerService;
    private WarnMapper mapper;


    private Media mediaTest;
    private Trigger triggerTest;
    private Warn warnTest;
    private WarnDTO warnDTOTest;

    @BeforeEach
    void initTests() {
        mediaService = Mockito.mock(MediaService.class);
        triggerService = Mockito.mock(TriggerService.class);
        mapper = new WarnMapper(mediaService, triggerService);

        mediaTest = new Media(2L, "Media test", "Description test", List.of());
        triggerTest = new Trigger(3L, "Trigger test", "Description test");
        warnTest = new Warn(triggerTest, mediaTest, 6);
        warnDTOTest = new WarnDTO(null, 2L, 3L, 6);
    }

    @Test
    void convertToDTOTest() {
        // GIVEN
        WarnDTO result;

        // WHEN
        result = mapper.toDTO(warnTest);

        // THEN
        assertThat(result).isEqualTo(warnDTOTest);
    }

    @Test
    void convertToDTOsTest() {
        // GIVEN
        List<Warn> warns = List.of(warnTest);
        List<WarnDTO> results;

        // WHEN
        results = mapper.toDTOs(warns);

        // THEN
        assertThat(results).containsExactly(warnDTOTest);
    }

    @Test
    void convertToEntityTest() {
        // GIVEN
        Warn result;

        when(mediaService.getMedia(any()))
                .thenReturn(CompletableFuture.completedFuture(mediaTest));
        when(triggerService.getTrigger(any()))
                .thenReturn(CompletableFuture.completedFuture(triggerTest));
        // WHEN
        result = mapper.toEntity(warnDTOTest);

        // THEN
        verify(mediaService).getMedia(2L);
        verify(triggerService).getTrigger(3L);
        assertThat(result).isEqualTo(warnTest);
    }

    @Test
    void convertToInvalidEntityTest() {
        // GIVEN
        when(mediaService.getMedia(any()))
                .thenReturn(CompletableFuture.completedFuture(mediaTest));
        when(triggerService.getTrigger(any()))
                .thenThrow(EntityException.EntityNotFoundException.class);
        // WHEN
        assertThatExceptionOfType(EntityException.EntityNotFoundException.class)
                .isThrownBy(() -> mapper.toEntity(warnDTOTest));

        // THEN
        verify(mediaService).getMedia(2L);
        verify(triggerService).getTrigger(3L);
    }

}
