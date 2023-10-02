package fr.backendt.cinephobia.services.integration;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.services.WarnService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletionException;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Transactional
@SpringBootTest
class WarnServiceIT {

    @Autowired
    private WarnService service;

    @Test
    void createWarnTest() throws EntityException {
        // GIVEN
        Trigger trigger = new Trigger();
        trigger.setId(1L);
        Media media = new Media();
        media.setId(1L);

        Warn warn = new Warn(trigger, media, 9);
        Warn result;

        // WHEN
        result = service.createWarn(warn).join();

        // THEN
        assertThat(result).isNotNull()
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void createWarnWithUnknownMediaTest() {
        // GIVEN
        Trigger trigger = new Trigger();
        trigger.setId(1L);
        Media unknownMedia = new Media();
        unknownMedia.setId(1337L);

        Warn invalidWarn = new Warn(trigger, unknownMedia, 5);

        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createWarn(invalidWarn).join())
                .withCauseExactlyInstanceOf(EntityException.class);
    }

    @Test
    void createWarnWithUnknownTriggerTest() {
        // GIVEN
        Trigger unknownTrigger = new Trigger();
        unknownTrigger.setId(1337L);
        Media media = new Media();
        media.setId(1L);

        Warn invalidWarn = new Warn(unknownTrigger, media, 5);

        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createWarn(invalidWarn).join())
                .withCauseExactlyInstanceOf(EntityException.class);
    }

    @Test
    void getAllWarnsTest() {
        // GIVEN
        List<Warn> results;

        // WHEN
        results = service.getAllWarns().join();

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0)).isNotNull()
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void getWarnsByMediaIdTest() {
        // GIVEN
        Long mediaId = 1L;
        List<Warn> results;

        // WHEN
        results = service.getWarnsByMediaId(mediaId).join();

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0)).isNotNull()
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void getNoWarnsByMediaIdTest() {
        // GIVEN
        Long unknownMediaId = 1337L;
        List<Warn> results;

        // WHEN
        results = service.getWarnsByMediaId(unknownMediaId).join();

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void getWarnByIdTest() throws EntityNotFoundException {
        // GIVEN
        Long warnId = 1L;
        Warn result;

        // WHEN
        result = service.getWarn(warnId).join();

        // THEN
        assertThat(result).isNotNull()
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void getUnknownWarnByIdTest() {
        // GIVEN
        Long unknownWarnId = 1337L;

        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getWarn(unknownWarnId).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);
    }

}
