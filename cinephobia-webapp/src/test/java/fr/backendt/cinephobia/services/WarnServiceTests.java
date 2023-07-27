package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.ModelException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.repositories.WarnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.List;
import java.util.Optional;

import static fr.backendt.cinephobia.exceptions.ModelException.ModelNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WarnServiceTests {

    private WarnRepository repository;
    private WarnService service;

    private Warn testWarn;

    @BeforeEach
    void initTests() {
        repository = Mockito.mock(WarnRepository.class);
        service = new WarnService(repository);

        Trigger testTrigger = new Trigger("Trigger", "Trigger");
        Media testMedia = new Media("Media", "https://example.com/media.png", List.of());
        testWarn = new Warn(1L, testTrigger, testMedia, 9);
    }

    @Test
    void createWarnTest() {
        // GIVEN
        Warn expected = new Warn(testWarn);
        expected.setId(null);
        Warn result;

        when(repository.save(any())).thenReturn(testWarn);
        // WHEN
        result = service.createWarn(testWarn).join();

        // THEN
        verify(repository).save(expected);
        assertThat(result).isEqualTo(testWarn);
    }

    @Test
    void failToCreateWarnWithUnsavedChildsTest() {
        // GIVEN
        Warn expected = new Warn(testWarn);
        expected.setId(null);

        when(repository.save(any()))
                .thenThrow(InvalidDataAccessApiUsageException.class);
        // WHEN
        // THEN
        assertThatExceptionOfType(ModelException.class)
                .isThrownBy(() -> service.createWarn(testWarn));
        verify(repository).save(expected);
    }

    @Test
    void failToCreateWarnWithUnknownChildsTest() {
        // GIVEN
        Warn expected = new Warn(testWarn);
        expected.setId(null);

        when(repository.save(any()))
                .thenThrow(DataIntegrityViolationException.class);
        // WHEN
        // THEN
        assertThatExceptionOfType(ModelException.class)
                .isThrownBy(() -> service.createWarn(testWarn));
        verify(repository).save(expected);
    }

    @Test
    void getAllWarnsTest() {
        // GIVEN
        List<Warn> warns = List.of(testWarn);
        List<Warn> results;

        when(repository.findAll()).thenReturn(warns);
        // WHEN
        results = service.getAllWarns().join();

        // THEN
        verify(repository).findAll();
        assertThat(results).containsExactlyElementsOf(warns);
    }

    @Test
    void getWarnsByMediaIdTest() {
        // GIVEN
        Long mediaId = 1L;
        List<Warn> warns = List.of(testWarn);
        List<Warn> results;

        when(repository.findAllByMediaId(any())).thenReturn(warns);
        // WHEN
        results = service.getWarnsByMediaId(mediaId).join();

        // THEN
        verify(repository).findAllByMediaId(mediaId);
        assertThat(results).containsExactlyElementsOf(warns);
    }

    @Test
    void getWarnByIdTest() {
        // GIVEN
        Long warnId = 1L;
        Warn result;

        when(repository.findById(any())).thenReturn(Optional.of(testWarn));
        // WHEN
        result = service.getWarn(warnId).join();

        // THEN
        verify(repository).findById(warnId);
        assertThat(result).isEqualTo(testWarn);
    }

    @Test
    void getWarnByUnknownIdTest() {
        // GIVEN
        Long warnId = 1L;

        when(repository.findById(any())).thenReturn(Optional.empty());
        // WHEN
        // THEN
        assertThatExceptionOfType(ModelNotFoundException.class)
                .isThrownBy(() -> service.getWarn(warnId));
        verify(repository).findById(warnId);
    }

}
