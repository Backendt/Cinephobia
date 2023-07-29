package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Platform;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
class MediaRepositoryTests {

    @Autowired
    private MediaRepository repository;

    @Autowired
    private PlatformRepository platformRepository;

    @Test
    void createMediaTest() {
        // GIVEN
        Platform savedPlatform = platformRepository.findById(1L).orElseThrow();
        Media unsavedMedia = new Media("Java Testing: 2", "https://example.com/hello.png", List.of(savedPlatform));
        Media result;

        // WHEN
        result = repository.save(unsavedMedia);

        // THEN
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void failToCreateMediaWithoutPlatformTest() {
        // GIVEN
        Media invalidMedia = new Media("Oh no!", "https://example.com/oops.png", List.of());

        // WHEN
        // THEN
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> repository.save(invalidMedia))
                .withMessageContaining("platform");
    }

    @Test
    void failToCreateDuplicateMediaTest() {
        // GIVEN
        Platform platform = platformRepository.findById(1L).orElseThrow();
        Media media = new Media("Cinephobia: The Revenge", "https://example.com/hey.png", List.of(platform));

        // WHEN
        // THEN
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> repository.save(media));
    }

    @Test
    void failToCreateMediaWithHttpUrlTest() {
        // GIVEN
        Platform savedPlatform = platformRepository.findById(1L).orElseThrow();
        Media unsavedMedia = new Media("Oh no! 2", "http://example.com/hello.png", List.of(savedPlatform));

        // WHEN
        // THEN
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> repository.save(unsavedMedia))
                .withMessageContaining("url");
    }

    @Test
    void getAllMediasTest() {
        // GIVEN
        List<Media> results;

        // WHEN
        results = repository.findAll();

        // THEN
        assertThat(results).isNotEmpty();
    }

    @Test
    void getMediaByIdTest() {
        // GIVEN
        Long mediaId = 1L;
        Optional<Media> result;

        // WHEN
        result = repository.findById(mediaId);

        // THEN
        assertThat(result).isNotEmpty();
        assertThat(result.get()).hasNoNullFieldsOrProperties();
        assertThat(result.get().getPlatforms()).isNotEmpty();
    }

    @Test
    void getMediasContainingTitleTest() {
        // GIVEN
        String titlePart = "PHOBIA";
        String fullTitle = "Cinephobia: The Revenge";
        List<Media> results;

        // WHEN
        results = repository.findAllByTitleContainingIgnoreCase(titlePart);

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).isEqualTo(fullTitle);
    }

    @Test
    void failToGetMediasContainingTitleTest() {
        // GIVEN
        List<Media> results;
        String nonexistentTitlePart = "hey";

        // WHEN
        results = repository.findAllByTitleContainingIgnoreCase(nonexistentTitlePart);

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void deleteMediaTest() {
        // GIVEN
        Long mediaId = 1L;
        Optional<Media> resultBefore;
        Optional<Media> resultAfter;

        // WHEN
        resultBefore = repository.findById(mediaId);
        repository.deleteById(mediaId);
        resultAfter = repository.findById(mediaId);

        // THEN
        assertThat(resultBefore).isNotEmpty();
        assertThat(resultAfter).isEmpty();
    }

}
