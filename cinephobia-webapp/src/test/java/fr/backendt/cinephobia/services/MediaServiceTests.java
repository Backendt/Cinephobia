package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.repositories.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class MediaServiceTests {

    private MediaRepository repository;
    private MediaService service;

    private Media testMedia;

    @BeforeEach
    void initTests() {
        repository = Mockito.mock(MediaRepository.class);
        service = new MediaService(repository);

        testMedia = new Media("Media service: The movie", "https://example.com/media.png");
    }


    @Test
    void createMediaTest() throws EntityException {
        // GIVEN
        Media expected = new Media(testMedia);
        testMedia.setId(1L);

        when(repository.exists(any()))
                .thenReturn(false);
        // WHEN
        service.createMedia(testMedia).join();

        // THEN
        verify(repository).exists(Example.of(testMedia));
        verify(repository).save(expected); // "expected" has null id
    }

    @Test
    void createExistingMediaTest() {
        // GIVEN
        when(repository.save(any()))
                .thenThrow(DataIntegrityViolationException.class);

        when(repository.exists(any()))
                .thenReturn(true);
        // THEN
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createMedia(testMedia).join())
                .withCauseExactlyInstanceOf(EntityException.class);

        verify(repository).exists(Example.of(testMedia));
        verify(repository, never()).save(any());
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
        PageRequest pageRequest = PageRequest.ofSize(99);
        Page<Media> returnedPage = new PageImpl<>(List.of(testMedia));
        Page<Media> results;

        when(repository.findAllByTitleContainingIgnoreCase(any(), any()))
                .thenReturn(returnedPage);
        // WHEN
        results = service.getMediaPage(mediaTitlePart, pageRequest).join();

        // THEN
        verify(repository).findAllByTitleContainingIgnoreCase(mediaTitlePart, pageRequest);
        assertThat(results).containsExactly(testMedia);
    }

    @Test
    void deleteMediaTest() {
        // GIVEN
        Long mediaId = 1L;

        when(repository.existsById(any()))
                .thenReturn(true);
        // WHEN
        service.deleteMedia(mediaId).join();

        // THEN
        verify(repository).existsById(mediaId);
        verify(repository).deleteById(mediaId);
    }

    @Test
    void deleteUnknownMediaTest() {
        // GIVEN
        Long mediaId = 1L;

        when(repository.existsById(any()))
                .thenReturn(false);
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.deleteMedia(mediaId).join())
                .withCauseExactlyInstanceOf(EntityException.EntityNotFoundException.class);

        // THEN
        verify(repository).existsById(mediaId);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void updateMediaTest() {
        // GIVEN
        long mediaId = 1L;
        Media mediaUpdate = new Media(null, "New title", null);
        Media currentMedia = new Media(mediaId, "Old title", "https://wedontcare.com");

        Media expectedResult = new Media(mediaId, "New title", "https://wedontcare.com");
        Media result;

        when(repository.findById(any())).thenReturn(Optional.of(currentMedia));
        when(repository.save(any())).thenReturn(expectedResult);
        // WHEN
        result = service.updateMedia(mediaId, mediaUpdate).join();

        // THEN
        verify(repository).findById(mediaId);
        verify(repository).save(expectedResult);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void updateUnknownMediaTest() {
        // GIVEN
        long mediaId = 1L;
        Media mediaUpdate = new Media(null, "New title", null);

        when(repository.findById(any())).thenReturn(Optional.empty());
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateMedia(mediaId, mediaUpdate).join())
                .withCauseExactlyInstanceOf(EntityException.EntityNotFoundException.class);

        // THEN
        verify(repository).findById(mediaId);
        verify(repository, never()).save(any());
    }

}
