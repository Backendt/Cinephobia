package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.*;
import fr.backendt.cinephobia.repositories.WarnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class WarnServiceTests {

    private WarnRepository repository;
    private WarnService service;

    private Warn testWarn;
    private Trigger testTrigger;
    private Media testMedia;
    private User testUser;

    @BeforeEach
    void initTests() {
        repository = Mockito.mock(WarnRepository.class);
        service = new WarnService(repository);

        testTrigger = new Trigger(1L, "Testphobia", "Fear of unit tests failing");
        testMedia = new Media(1234L, MediaType.MOVIE, "1234 Movie", "The 1234 Movie", "https://1234.com/poster");
        testUser = new User(2L, "Jane Doe", "jane.doe@test.com", "myPassword1234", "USER");
        testWarn = new Warn(testTrigger, testUser, testMedia, 9);
    }

    @Test
    void createWarnTest() {
        // GIVEN
        Long warnUserId = testUser.getId();
        Long triggerId = testTrigger.getId();
        Long mediaId = testMedia.getId();
        MediaType mediaType = testMedia.getType();

        boolean alreadyExists = false;

        Warn result;

        when(repository.existsByUserIdAndTriggerIdAndMediaIdAndMediaType(any(), any(), any(), any()))
                .thenReturn(alreadyExists);
        when(repository.save(any())).thenReturn(testWarn);
        // WHEN
        result = service.createWarn(testWarn).join();

        // THEN
        verify(repository).existsByUserIdAndTriggerIdAndMediaIdAndMediaType(warnUserId, triggerId, mediaId, mediaType);
        verify(repository).save(testWarn);

        assertThat(result).isEqualTo(testWarn);
    }

    @Test
    void createDuplicateWarnTest() {
        // GIVEN
        Long warnUserId = testUser.getId();
        Long triggerId = testTrigger.getId();
        Long mediaId = testMedia.getId();
        MediaType mediaType = testMedia.getType();

        boolean alreadyExists = true;

        when(repository.existsByUserIdAndTriggerIdAndMediaIdAndMediaType(any(), any(), any(), any()))
                .thenReturn(alreadyExists);
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createWarn(testWarn).join())
                .withCauseExactlyInstanceOf(BadRequestException.class);

        // THEN
        verify(repository).existsByUserIdAndTriggerIdAndMediaIdAndMediaType(warnUserId, triggerId, mediaId, mediaType);
        verify(repository, never()).save(testWarn);
    }

    @Test
    void getWarnsForMediaTest() {
        // GIVEN
        long mediaId = 1234L;
        MediaType mediaType = MediaType.MOVIE;
        Pageable pageable = Pageable.unpaged();

        Page<Warn> warns = new PageImpl<>(List.of(testWarn));
        Page<Warn> result;

        when(repository.findAllByMediaIdAndMediaType(any(), any(), any()))
                .thenReturn(warns);

        // WHEN
        result = service.getWarnsForMedia(mediaId, mediaType, pageable).join();

        // THEN
        verify(repository).findAllByMediaIdAndMediaType(mediaId, mediaType, pageable);
        assertThat(result).containsExactly(testWarn);
    }

    @Test
    void getWarnsForUserTest() {
        // GIVEN
        String userEmail = "user@test.com";
        Pageable pageable = Pageable.unpaged();

        Page<Warn> warns = new PageImpl<>(List.of(testWarn));

        Page<Warn> results;
        when(repository.findAllByUserEmail(any(), any())).thenReturn(warns);
        // WHEN
        results = service.getWarnsForUser(userEmail, pageable).join();

        // THEN
        verify(repository).findAllByUserEmail(userEmail, pageable);
        assertThat(results).containsExactly(testWarn);
    }

    @Test
    void getWarnByIdTest() {
        // GIVEN
        long warnId = 1L;

        Optional<Warn> warn = Optional.of(testWarn);
        Warn result;

        when(repository.findById(any())).thenReturn(warn);
        // WHEN
        result = service.getWarn(warnId).join();

        // THEN
        verify(repository).findById(warnId);
        assertThat(result).isEqualTo(testWarn);
    }

    @Test
    void getUnknownWarnByIdTest() {
        // GIVEN
        long unknownWarnId = 1L;

        Optional<Warn> warn = Optional.empty();

        when(repository.findById(any())).thenReturn(warn);
        // WHEN

        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getWarn(unknownWarnId).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        // THEN
        verify(repository).findById(unknownWarnId);
    }

    @Test
    void updateWarnTest() {
        // GIVEN
        long warnId = 1L;
        int newExpositionLevel = 5;
        Warn warnUpdate = new Warn(null, null, null, null, newExpositionLevel);

        Warn expectedWarn = new Warn(testWarn);
        expectedWarn.setExpositionLevel(newExpositionLevel);

        Warn result;

        when(repository.findById(any())).thenReturn(Optional.of(testWarn));
        when(repository.save(any())).thenReturn(expectedWarn);
        // WHEN
        result = service.updateWarn(warnId, warnUpdate).join();

        // THEN
        verify(repository).findById(warnId);
        verify(repository, never()).existsByUserIdAndTriggerIdAndMediaIdAndMediaType(any(), any(), any(), any());
        verify(repository).save(expectedWarn);
        assertThat(result).isEqualTo(expectedWarn);
    }

    @Test
    void updateUnknownWarnTest() {
        // GIVEN
        long warnId = 1L;
        int newExpositionLevel = 5;
        Warn warnUpdate = new Warn(null, null, null, null, newExpositionLevel);

        when(repository.findById(any())).thenReturn(Optional.empty());
        // WHEN

        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateWarn(warnId, warnUpdate).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        // THEN
        verify(repository).findById(warnId);
        verify(repository, never()).existsByUserIdAndTriggerIdAndMediaIdAndMediaType(any(), any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void updateUniqueFieldsTest() {
        // GIVEN
        long warnId = 1L;
        long newMediaId = 4321L;
        int newExpositionLevel = 5;
        Warn warnUpdate = new Warn(null, null, newMediaId, null, newExpositionLevel);

        Long currentUserId = testUser.getId();
        Long currentTriggerId = testTrigger.getId();
        MediaType currentMediaType = testWarn.getMediaType();

        boolean newWarnAlreadyExists = false;

        Warn expectedWarn = new Warn(testWarn);
        expectedWarn.setExpositionLevel(newExpositionLevel);
        expectedWarn.setMediaId(newMediaId);

        Warn result;

        when(repository.findById(any())).thenReturn(Optional.of(testWarn));
        when(repository.existsByUserIdAndTriggerIdAndMediaIdAndMediaType(any(), any(), any(), any()))
                .thenReturn(newWarnAlreadyExists);
        when(repository.save(any())).thenReturn(expectedWarn);
        // WHEN

        result = service.updateWarn(warnId, warnUpdate).join();

        // THEN
        verify(repository).findById(warnId);
        verify(repository).existsByUserIdAndTriggerIdAndMediaIdAndMediaType(currentUserId, currentTriggerId, newMediaId, currentMediaType);
        verify(repository).save(expectedWarn);
        assertThat(result).isEqualTo(expectedWarn);
    }

    @Test
    void updateWarnToDuplicateTest() {
        // GIVEN
        long warnId = 1L;
        long newMediaId = 4321L;
        int newExpositionLevel = 5;
        Warn warnUpdate = new Warn(null, null, newMediaId, null, newExpositionLevel);

        Long currentUserId = testUser.getId();
        Long currentTriggerId = testTrigger.getId();
        MediaType currentMediaType = testWarn.getMediaType();

        boolean newWarnAlreadyExists = true;

        Warn expectedWarn = new Warn(testWarn);
        expectedWarn.setExpositionLevel(newExpositionLevel);
        expectedWarn.setMediaId(newMediaId);

        when(repository.findById(any())).thenReturn(Optional.of(testWarn));
        when(repository.existsByUserIdAndTriggerIdAndMediaIdAndMediaType(any(), any(), any(), any()))
                .thenReturn(newWarnAlreadyExists);
        when(repository.save(any())).thenReturn(expectedWarn);
        // WHEN

        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateWarn(warnId, warnUpdate).join())
                .withCauseExactlyInstanceOf(BadRequestException.class);

        // THEN
        verify(repository).findById(warnId);
        verify(repository).existsByUserIdAndTriggerIdAndMediaIdAndMediaType(currentUserId, currentTriggerId, newMediaId, currentMediaType);
        verify(repository, never()).save(any());
    }

    @Test
    void updateWarnIfOwnedByUserTest() {
        // GIVEN
        long warnId = 1L;
        int newExpositionLevel = 5;
        Warn warnUpdate = new Warn(null, null, null, null, newExpositionLevel);

        String ownerEmail = testUser.getEmail();
        boolean isOwnedByUser = true;

        Warn expectedWarn = new Warn(testWarn);
        expectedWarn.setExpositionLevel(newExpositionLevel);

        Warn result;

        when(repository.existsByIdAndUserEmail(any(), any())).thenReturn(isOwnedByUser);
        when(repository.findById(any())).thenReturn(Optional.of(testWarn));
        when(repository.save(any())).thenReturn(expectedWarn);
        // WHEN
        result = service.updateWarnIfOwnedByUser(warnId, warnUpdate, ownerEmail).join();

        // THEN
        verify(repository).existsByIdAndUserEmail(warnId, ownerEmail);
        verify(repository).findById(warnId);
        verify(repository, never()).existsByUserIdAndTriggerIdAndMediaIdAndMediaType(any(), any(), any(), any());
        verify(repository).save(expectedWarn);
        assertThat(result).isEqualTo(expectedWarn);
    }

    @Test
    void updateWarnIfOwnedByOtherUserTest() {
        // GIVEN
        long warnId = 1L;
        int newExpositionLevel = 5;
        Warn warnUpdate = new Warn(null, null, null, null, newExpositionLevel);

        String ownerEmail = testUser.getEmail();
        boolean isOwnedByUser = false;

        Warn expectedWarn = new Warn(testWarn);
        expectedWarn.setExpositionLevel(newExpositionLevel);

        when(repository.existsByIdAndUserEmail(any(), any())).thenReturn(isOwnedByUser);
        when(repository.findById(any())).thenReturn(Optional.of(testWarn));
        when(repository.save(any())).thenReturn(expectedWarn);
        // WHEN

        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateWarnIfOwnedByUser(warnId, warnUpdate, ownerEmail).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        // THEN
        verify(repository).existsByIdAndUserEmail(warnId, ownerEmail);
        verify(repository, never()).findById(warnId);
        verify(repository, never()).existsByUserIdAndTriggerIdAndMediaIdAndMediaType(any(), any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteWarnTest() {
        // GIVEN
        long warnId = 1L;
        boolean warnExists = true;

        when(repository.existsById(any())).thenReturn(warnExists);
        // WHEN
        service.deleteWarn(warnId).join();

        // THEN
        verify(repository).existsById(warnId);
        verify(repository).deleteById(warnId);
    }

    @Test
    void deleteUnknownWarnTest() {
        // GIVEN
        long warnId = 1L;
        boolean warnExists = false;

        when(repository.existsById(any())).thenReturn(warnExists);
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.deleteWarn(warnId).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        // THEN
        verify(repository).existsById(warnId);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteWarnIfOwnedByUserTest() {
        // GIVEN
        long warnId = 1L;
        String ownerEmail = testUser.getEmail();
        boolean warnExists = true;

        when(repository.existsByIdAndUserEmail(any(), any())).thenReturn(warnExists);
        // WHEN
        service.deleteWarnIfOwnedByUser(warnId, ownerEmail).join();

        // THEN
        verify(repository).existsByIdAndUserEmail(warnId, ownerEmail);
        verify(repository).deleteById(warnId);
    }

    @Test
    void deleteUnknownWarnIfOwnedByUserTest() {
        // GIVEN
        long warnId = 1L;
        String ownerEmail = testUser.getEmail();
        boolean warnExists = false;

        when(repository.existsByIdAndUserEmail(any(), any())).thenReturn(warnExists);
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.deleteWarnIfOwnedByUser(warnId, ownerEmail).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        // THEN
        verify(repository).existsByIdAndUserEmail(warnId, ownerEmail);
        verify(repository, never()).deleteById(any());
    }

}
