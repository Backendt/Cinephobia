package fr.backendt.cinephobia.services.integration;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.services.MediaService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.concurrent.CompletionException;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Transactional
@SpringBootTest
class MediaServiceIT {

    @Autowired
    private MediaService service;

    @Test
    void createMediaTest() throws EntityException {
        // GIVEN
        Media media = new Media("NewMedia", "https://example.com/media.png");
        Media result;

        // WHEN
        result = service.createMedia(media).join();

        // THEN
        assertThat(result).isNotNull()
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void createDuplicateMediaTest() {
        // GIVEN
        Media duplicateMedia = new Media("Cinephobia: The Revenge", "https://example.com/media.png");

        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createMedia(duplicateMedia).join())
                .withCauseExactlyInstanceOf(EntityException.class);
    }

    @Test
    void getMediasContainingInTitleTest() {
        // GIVEN
        String titlePart = "Venge";
        String expectedTitle = "Cinephobia: The Revenge";
        PageRequest pageRequest = PageRequest.ofSize(99);
        Page<Media> results;

        // WHEN
        results = service.getMediaPage(titlePart, pageRequest).join();

        // THEN
        assertThat(results)
                .hasSize(1);
        assertThat(results.get().findFirst()).isPresent();
        assertThat(results.get().findFirst().get().getTitle()).isEqualTo(expectedTitle);
    }

    @Test
    void getNoMediasContainingInTitleTest() {
        // GIVEN
        String titlePart = "cinepobia";
        PageRequest pageRequest = PageRequest.ofSize(99);
        Page<Media> results;

        // WHEN
        results = service.getMediaPage(titlePart, pageRequest).join();

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void getMediaByIdTest() throws EntityNotFoundException {
        // GIVEN
        Long mediaId = 1L;
        Media result;

        // WHEN
        result = service.getMedia(mediaId).join();

        // THEN
        assertThat(result).isNotNull()
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void getUnknownMediaByIdTest() {
        // GIVEN
        Long unknownMediaId = 1337L;

        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getMedia(unknownMediaId).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);
    }

}
