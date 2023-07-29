package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.Warn;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
class WarnRepositoryTests {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private TriggerRepository triggerRepository;

    @Autowired
    private WarnRepository repository;

    // Create
    @Test
    void createWarnTest() {
        // GIVEN
        Trigger trigger = triggerRepository.findById(1L).orElseThrow();
        Media media = mediaRepository.findById(1L).orElseThrow();
        Warn warn = new Warn(trigger, media, 9);
        Warn result;

        // WHEN
        result = repository.save(warn);

        // THEN
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void failToCreateWarnWithUnsavedTriggerTest() {
        // GIVEN
        Trigger trigger = new Trigger( "Not saved", "Trigger not saved");
        Media media = mediaRepository.findById(1L).orElseThrow();
        Warn warn = new Warn(trigger, media, 9);

        // THEN
        // WHEN
        assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
                .isThrownBy(() -> repository.save(warn));
    }

    @Test
    void failToCreateWarnWithInvalidTriggerTest() {
        // GIVEN
        Trigger trigger = new Trigger(1337L, "Not saved", "Trigger not saved");
        Media media = mediaRepository.findById(1L).orElseThrow();
        Warn warn = new Warn(trigger, media, 9);

        // THEN
        // WHEN
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> repository.save(warn));
    }

    @Test
    void failToCreateWarnWithInvalidMediaTest() {
        // GIVEN
        Trigger trigger = triggerRepository.findById(1L).orElseThrow();
        Media media = new Media(1337L, "Not saved", "https://example.com/hey.png", List.of());
        Warn warn = new Warn(trigger, media, 9);

        // THEN
        // WHEN
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> repository.save(warn));
    }

    @Test
    void getWarnByIdTest() {
        // GIVEN
        Long warnId = 1L;
        Optional<Warn> result;

        // WHEN
        result = repository.findById(warnId);

        // THEN
        assertThat(result).isNotEmpty();
        assertThat(result.get()).hasNoNullFieldsOrProperties();
    }

    @Test
    void getAllWarnsTest() {
        // GIVEN
        List<Warn> results;

        // WHEN
        results = repository.findAll();

        // THEN
        assertThat(results).isNotEmpty();
    }

    @Test
    void getWarnsByMediaIdTest() {
        // GIVEN
        Long mediaId = 1L;
        List<Warn> results;

        // WHEN
        results = repository.findAllByMediaId(mediaId);

        // THEN
        assertThat(results).isNotEmpty();
    }

    @Test
    void deleteWarnByIdTest() {
        // GIVEN
        Long warnId = 1L;
        Optional<Warn> resultBefore;
        Optional<Warn> resultAfter;

        // WHEN
        resultBefore = repository.findById(warnId);
        repository.deleteById(warnId);
        resultAfter = repository.findById(warnId);

        // THEN
        assertThat(resultBefore).isNotEmpty();
        assertThat(resultAfter).isEmpty();
    }

}
