package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Platform;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MediaRepositoryTests {

    @Autowired
    private MediaRepository repository;

    @Autowired
    private PlatformRepository platformRepository;

    private Media testMedia;

    @BeforeEach
    void initTestValues() {
        Platform savedTestPlatform = platformRepository.save(new Platform("JUnit TV"));
        testMedia = new Media("Java Testing: The Revenge", "https://example.com/hello.png", List.of(savedTestPlatform));
    }

    @Test
    void createMediaTest() {
        // GIVEN
        Media result;

        // WHEN
        result = repository.save(testMedia);

        // THEN
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void getAllMediasTest() {
        // GIVEN
        List<Media> resultsBefore;
        List<Media> resultsAfter;

        // WHEN
        resultsBefore = repository.findAll();
        repository.save(testMedia);
        resultsAfter = repository.findAll();

        // THEN
        assertThat(resultsBefore).isEmpty();
        assertThat(resultsAfter).isNotEmpty();
    }

    @Test
    void getMediaByIdTest() {
        // GIVEN
        Media expected;
        Optional<Media> result;

        // WHEN
        expected = repository.save(testMedia);
        result = repository.findById(expected.getId());

        // THEN
        assertThat(result).contains(expected);
    }

    @Test
    void getMediasContainingTitleTest() {
        // GIVEN
        Media expected;
        List<Media> results;
        String titlePart = testMedia.getTitle()
                .toUpperCase()
                .substring(2, 6);

        // WHEN
        expected = repository.save(testMedia);
        results = repository.findAllByTitleContainingIgnoreCase(titlePart);

        // THEN
        assertThat(results).containsExactly(expected);
    }

    @Test
    void failToGetMediasContainingTitleTest() {
        // GIVEN
        List<Media> results;
        String nonexistentTitlePart = "hey";

        // WHEN
        repository.save(testMedia);
        results = repository.findAllByTitleContainingIgnoreCase(nonexistentTitlePart);

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void deleteMediaTest() {
        // GIVEN
        Media savedMedia;
        Optional<Media> resultBefore;
        Optional<Media> resultAfter;

        // WHEN
        savedMedia = repository.save(testMedia);
        resultBefore = repository.findById(savedMedia.getId());
        repository.deleteById(savedMedia.getId());
        resultAfter = repository.findById(savedMedia.getId());

        // THEN
        assertThat(resultBefore).isNotEmpty();
        assertThat(resultAfter).isEmpty();
    }

}
