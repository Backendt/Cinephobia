package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.Warn;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WarnRepositoryTests {

    @Autowired
    private WarnRepository repository;

    private Warn warn;

    @BeforeEach
    void initTests() {
        Trigger trigger = new Trigger(1L, "Testphobia", "Fear of unit tests failing");
        User user = new User(1L, "John doe", "john.doe@test.com", "HASHED", "USER");
        warn = new Warn(1L, trigger, user,1L, MediaType.MOVIE, 9);
    }

    // Create
    @Test
    void createWarnTest() {
        // GIVEN
        Warn result;

        // WHEN
        result = repository.save(warn);

        // THEN
        assertThat(result).hasNoNullFieldsOrProperties();
    }

    @Test
    void createDuplicateWarnTest() {
        // GIVEN
        Trigger trigger = new Trigger(2L, "Bugphobia", "Fear of software bugs");
        User user = new User(1L, "John Doe", "john.doe@test.com", "John1234", "USER");
        long mediaId = 1L;
        Warn duplicateWarn = new Warn(trigger, user, mediaId, MediaType.MOVIE, 5);

        // WHEN
        // THEN
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> repository.save(duplicateWarn))
                .withCauseExactlyInstanceOf(ConstraintViolationException.class);
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
        Page<Warn> results;

        // WHEN
        results = repository.findAll(Pageable.unpaged());

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.stream().findAny()).isPresent();
        assertThat(results.stream().findAny().orElseThrow()).hasNoNullFieldsOrProperties();
    }

    @Test
    void getWarnsByMediaTest() {
        // GIVEN
        long mediaId = 1L;
        MediaType mediaType = MediaType.MOVIE;
        Page<Warn> results;

        // WHEN
        results = repository.findAllByMediaIdAndMediaType(mediaId, mediaType, Pageable.unpaged());

        // THEN
        assertThat(results).isNotEmpty();
    }

    @Test
    void getWarnsByUnknownMediaTest() {
        // GIVEN
        long mediaId = 0L;
        MediaType mediaType = MediaType.MOVIE;
        Page<Warn> results;

        // WHEN
        results = repository.findAllByMediaIdAndMediaType(mediaId, mediaType, Pageable.unpaged());

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void getWarnsByUserEmailTest() {
        // GIVEN
        String userEmail = "john.doe@test.com";
        Page<Warn> results;

        // WHEN
        results = repository.findAllByUserEmail(userEmail, Pageable.unpaged());

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.getContent().get(0)).hasNoNullFieldsOrProperties();
    }

    @Test
    void getWarnsByUnknownUserEmailTest() {
        // GIVEN
        String userEmail = "unknown@test.com";
        Page<Warn> results;

        // WHEN
        results = repository.findAllByUserEmail(userEmail, Pageable.unpaged());

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    void deleteWarnByIdTest() {
        // GIVEN
        Long warnId = 1L;
        boolean existsBefore, existsAfter;

        // WHEN
        existsBefore = repository.existsById(warnId);
        repository.deleteById(warnId);
        existsAfter = repository.existsById(warnId);

        // THEN
        assertThat(existsBefore).isTrue();
        assertThat(existsAfter).isFalse();
    }

    @Test
    void existsByUniqueFieldsTest() {
        // GIVEN
        long userId = 1;
        long triggerId = 2;
        long mediaId = 1;
        MediaType mediaType = MediaType.MOVIE;

        boolean result;
        // WHEN
        result = repository.existsByUserIdAndTriggerIdAndMediaIdAndMediaType(userId, triggerId, mediaId, mediaType);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    void doesNotExistsByUniqueFieldsTest() {
        // GIVEN
        long userId = 1;
        long triggerId = 1;
        long mediaId = 1;
        MediaType mediaType = MediaType.MOVIE;

        boolean result;
        // WHEN
        result = repository.existsByUserIdAndTriggerIdAndMediaIdAndMediaType(userId, triggerId, mediaId, mediaType);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    void getWarnOwnedByUserTest() {
        // GIVEN
        long warnId = 1L;
        String userEmail = "john.doe@test.com";

        Optional<Warn> result;
        // WHEN
        result = repository.findByIdAndUserEmail(warnId, userEmail);

        // THEN
        assertThat(result).isNotEmpty();
        assertThat(result.get()).hasNoNullFieldsOrProperties();
    }

    @Test
    void getUnknownWarnOwnedByUserTest() {
        // GIVEN
        long unknownWarnId = 2L;
        String userEmail = "john.doe@test.com";

        Optional<Warn> result;
        // WHEN
        result = repository.findByIdAndUserEmail(unknownWarnId, userEmail);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void getWarnOwnedByOtherUserTest() {
        // GIVEN
        long warnId = 1L;
        String otherUserEmail = "jane.doe@test.com";

        Optional<Warn> result;
        // WHEN
        result = repository.findByIdAndUserEmail(warnId, otherUserEmail);

        // THEN
        assertThat(result).isEmpty();
    }

}
