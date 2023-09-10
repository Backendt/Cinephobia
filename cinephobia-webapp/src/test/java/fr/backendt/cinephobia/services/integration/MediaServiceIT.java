package fr.backendt.cinephobia.services.integration;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.services.MediaService;
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
class MediaServiceIT {

    @Autowired
    private MediaService service;

    @Test
    void createMediaTest() {
        // GIVEN
        Platform platform = new Platform();
        platform.setId(1L);

        Media media = new Media("NewMedia", "https://example.com/media.png", List.of(platform));
        Media result;

        // WHEN
        result = service.createMedia(media).join();

        // THEN
        assertThat(result).isNotNull()
                .hasNoNullFieldsOrProperties();
        assertThat(result.getPlatforms()).isNotEmpty();
    }

    @Test
    void createDuplicateMediaTest() {
        // GIVEN
        Platform platform = new Platform();
        platform.setId(1L);
        Media duplicateMedia = new Media("Cinephobia: The Revenge", "https://example.com/media.png", List.of(platform));

        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createMedia(duplicateMedia).join())
                .withCauseExactlyInstanceOf(EntityException.class);
    }

    @Test
    void getAllMediaTest() {
        // GIVEN
        List<Media> results;

        // WHEN
        results = service.getAllMedias().join();

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0)).hasNoNullFieldsOrProperties();
    }

    @Test
    void getMediasContainingInTitleTest() {
        // GIVEN
        String titlePart = "venge";
        String expectedTitle = "Cinephobia: The Revenge";
        List<Media> results;

        // WHEN
        results = service.getMediaContainingInTitle(titlePart).join();

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).isEqualTo(expectedTitle);
    }

    @Test
    void getNoMediasContainingInTitleTest() {
        // GIVEN
        String titlePart = "venge";
        String expectedTitle = "Cinephobia: The Revenge";
        List<Media> results;

        // WHEN
        results = service.getMediaContainingInTitle(titlePart).join();

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).isEqualTo(expectedTitle);
    }

    @Test
    void getMediaByIdTest() {
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
