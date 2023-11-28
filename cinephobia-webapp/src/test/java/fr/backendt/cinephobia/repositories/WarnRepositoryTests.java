package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.Warn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WarnRepositoryTests {

    @Autowired
    private WarnRepository repository;

    private Warn warn;

    @BeforeEach
    void initTests() {
        Trigger trigger = new Trigger(1L, "Testphobia", "Fear of unit tests failing");
        warn = new Warn(1L, trigger, 1234L, MediaType.MOVIE, 9);
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
    void getWarnsByMediaIdTest() {
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

}
