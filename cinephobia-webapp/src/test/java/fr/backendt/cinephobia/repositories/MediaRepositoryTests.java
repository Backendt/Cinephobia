package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Media;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MediaRepositoryTests {

    @Autowired
    private MediaRepository repository;

    @Test
    void createMediaTest() {
        // GIVEN
        Media unsavedMedia = new Media("Java Testing: 2", "https://example.com/hello.png");
        Media result;

        // WHEN
        result = repository.save(unsavedMedia);

        // THEN
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void createMediaWithoutPlatformTest() {
        // GIVEN
        Media unsavedMedia = new Media("Java Testing: The return", "https://example.com/hello.png");
        Media result;

        // WHEN
        result = repository.save(unsavedMedia);

        // THEN
        assertThat(result.getId()).isNotNull();
        assertThat(result).hasNoNullFieldsOrProperties();
    }

    @Test
    void failToCreateDuplicateMediaTest() {
        // GIVEN
        Media media = new Media("Cinephobia: The Revenge", "https://example.com/hey.png");

        // WHEN
        // THEN
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> repository.save(media));
    }

    @Test
    void failToCreateMediaWithHttpUrlTest() {
        // GIVEN
        Media unsavedMedia = new Media("Oh no! 2", "http://example.com/hello.png");

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
    }

    @Test
    void getMediasContainingTitleTest() {
        // GIVEN
        String titlePart = "PHOBIA";
        String fullTitle = "Cinephobia: The Revenge";
        Page<Media> results;

        // WHEN
        results = repository.findAllByTitleContainingIgnoreCase(titlePart, PageRequest.ofSize(99));

        // THEN
        assertThat(results)
                .hasSize(1);
        assertThat(results.get().findFirst()).isPresent();
        assertThat(results.get().findFirst().get().getTitle()).isEqualTo(fullTitle);
    }

    @Test
    void failToGetMediasContainingTitleTest() {
        // GIVEN
        PageRequest pageRequest = PageRequest.ofSize(99);
        Page<Media> results;
        String nonexistentTitlePart = "hey";

        // WHEN
        results = repository.findAllByTitleContainingIgnoreCase(nonexistentTitlePart, pageRequest);

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

    @CsvSource({
            "Cinephobia: The Revenge",
            "cinephobia: The revenge"
    })
    @ParameterizedTest
    void existsByTitleTest(String title) {
        // GIVEN
        boolean result;

        // WHEN
        result = repository.existsByTitleIgnoreCase(title);

        // THEN
        assertThat(result).isTrue();
    }

    @CsvSource({
            "Cinephobia: The revengee",
            "Cinephobia the revenge",
            "Ci nephobia: The Revenge"
    })
    @ParameterizedTest
    void existsByUnknownTitleTest(String unknownTitle) {
        // GIVEN
        boolean result;

        // WHEN
        result = repository.existsByTitleIgnoreCase(unknownTitle);

        // THEN
        assertThat(result).isFalse();
    }

}
