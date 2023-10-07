package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.repositories.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MediaServiceTests {

    private MediaRepository repository;
    private MediaService service;

    private Media testMedia;

    @BeforeEach
    void initTests() {
        repository = Mockito.mock(MediaRepository.class);
        service = new MediaService(repository);

        Platform platform = new Platform("Testflix");
        testMedia = new Media("Media service: The movie", "https://example.com/media.png", List.of(platform));
    }


    @Test
    void createMediaTest() throws EntityException {
        // GIVEN
        Media expected = new Media(testMedia);
        testMedia.setId(1L);

        when(repository.existsByTitleIgnoreCase(any()))
                .thenReturn(false);
        // WHEN
        service.createMedia(testMedia).join();

        // THEN
        verify(repository).existsByTitleIgnoreCase(testMedia.getTitle());
        verify(repository).save(expected); // "expected" has null id
    }

    @Test
    void createExistingMediaTest() {
        // GIVEN
        when(repository.save(any()))
                .thenThrow(DataIntegrityViolationException.class);

        when(repository.existsByTitleIgnoreCase(any()))
                .thenReturn(true);
        // THEN
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createMedia(testMedia).join())
                .withCauseExactlyInstanceOf(EntityException.class);

        verify(repository).existsByTitleIgnoreCase(testMedia.getTitle());
        verify(repository, never()).save(any());
    }

    @Test
    void getAllMediaTest() {
        // GIVEN
        List<Media> medias = List.of(testMedia);
        List<Media> results;

        when(repository.findAll())
                .thenReturn(medias);
        // WHEN
        results = service.getAllMedias().join();

        // THEN
        verify(repository).findAll();
        assertThat(results).containsExactlyElementsOf(medias);
    }

    @Test
    void getMediaByIdTest() throws EntityException.EntityNotFoundException {
        // GIVEN
        Long mediaId = 1L;
        Media result;

        when(repository.findById(any())).thenReturn(Optional.of(testMedia));
        // WHEN
        result = service.getMedia(mediaId).join();

        // THEN
        verify(repository).findById(mediaId);
        assertThat(result).isEqualTo(testMedia);
    }

    @Test
    void getUnknownMediaByIdTest() {
        // GIVEN
        Long mediaId = 1L;

        when(repository.findById(any())).thenReturn(Optional.empty());
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getMedia(mediaId).join())
                .withCauseExactlyInstanceOf(EntityException.EntityNotFoundException.class);

        // THEN
        verify(repository).findById(mediaId);
    }

    @Test
    void getMediaByTitleContainingStringTest() {
        // GIVEN
        String mediaTitlePart = "media";
        List<Media> results;

        when(repository.findAllByTitleContainingIgnoreCase(any()))
                .thenReturn(List.of(testMedia));
        // WHEN
        results = service.getMediaContainingInTitle(mediaTitlePart).join();

        // THEN
        verify(repository).findAllByTitleContainingIgnoreCase(mediaTitlePart);
        assertThat(results).containsExactly(testMedia);
    }


}
