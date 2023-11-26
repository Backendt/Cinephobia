package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTests {

    @Autowired
    private UserRepository repository;

    private User userTest;

    @BeforeEach
    void initTest() {
        userTest = new User("Test User", "test@user.com", "password", "USER");
    }


    @Test
    void createUserTest() {
        // GIVEN
        User result;

        // WHEN
        result = repository.save(userTest);

        // THEN
        assertThat(result)
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("triggers");
    }

    @Test
    void createUserWithTriggerTest() {
        // GIVEN
        User result;
        Set<Trigger> triggers = Set.of(new Trigger(1L, null, null));
        userTest.setTriggers(triggers);

        // WHEN
        result = repository.save(userTest);

        // THEN
        assertThat(result)
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("triggers");
    }

    @Test
    void createDuplicateUserTest() {
        // GIVEN
        String duplicateEmail = "john.doe@test.com";
        User duplicateUser = new User("Fake John", duplicateEmail, "password", "USER");

        // WHEN
        // THEN
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> repository.save(duplicateUser));
    }

    @Test
    void getUsersTest() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 50);
        Page<User> results;

        // WHEN
        results = repository.findAll(pageable);

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.getContent().get(0)).hasNoNullFieldsOrProperties();
    }

    @Test
    void getUsersContainingInNameTest() {
        // GIVEN
        String nameSearch = "joh";
        Pageable pageable = PageRequest.of(0, 50);

        String expectedName = "John Doe";
        Page<User> results;

        // WHEN
        results = repository.findAllByDisplayNameContainingIgnoreCase(nameSearch, pageable);

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.getContent().get(0)).hasNoNullFieldsOrProperties();
        assertThat(results.getContent().get(0).getDisplayName()).isEqualTo(expectedName);
    }

    @Test
    void getUserByIdTest() {
        // GIVEN
        Optional<User> result;
        Long userId = 1L;

        // WHEN
        result = repository.findById(userId);

        // THEN
        assertThat(result).isNotEmpty();
        assertThat(result.get()).hasNoNullFieldsOrProperties();
    }

    @CsvSource({
            "john.doe@test.com",
            "JOHN.doe@TEST.com"
    })
    @ParameterizedTest
    void getUserByEmailTest(String userEmail) {
        // GIVEN
        Optional<User> result;

        // WHEN
        result = repository.findByEmailIgnoreCase(userEmail);

        // THEN
        assertThat(result).isNotEmpty();
        assertThat(result.get()).hasNoNullFieldsOrProperties();
    }

    @CsvSource({
            "jane.doe@test.com",
            "JANE.doe@TEST.com"
    })
    @ParameterizedTest
    void getUserWithRelationsTest(String userEmail) {
        // GIVEN
        Optional<User> result;

        // WHEN
        result = repository.findUserWithRelationsByEmail(userEmail);

        // THEN
        assertThat(result).isNotEmpty();
        assertThat(result.get()).hasNoNullFieldsOrProperties();
        assertThat(result.get().getTriggers()).isNotEmpty();
        assertThat(result.get().getTriggers().toArray()[0]).hasNoNullFieldsOrProperties();
    }

    @Test
    void deleteUserByIdTest() {
        // GIVEN
        Long userId = 1L;
        boolean existsBefore;
        boolean existsAfter;

        // WHEN
        existsBefore = repository.existsById(userId);
        repository.deleteById(userId);
        existsAfter = repository.existsById(userId);

        // THEN
        assertThat(existsBefore).isTrue();
        assertThat(existsAfter).isFalse();
    }

    @Test
    void deleteUserByEmailTest() {
        // GIVEN
        String email = "john.doe@test.com";
        boolean existsBefore;
        boolean existsAfter;

        // WHEN
        existsBefore = repository.existsByEmailIgnoreCase(email);
        repository.deleteByEmailIgnoreCase(email);
        existsAfter = repository.existsByEmailIgnoreCase(email);

        // THEN
        assertThat(existsBefore).isTrue();
        assertThat(existsAfter).isFalse();
    }

    @Test
    void getEmailByUserIdTest() {
        // GIVEN
        long userId = 1L;
        String expectedEmail = "john.doe@test.com";
        Optional<String> result;

        // WHEN
        result = repository.findEmailById(userId);

        // THEN
        assertThat(result).contains(expectedEmail);
    }

    @CsvSource({
            "0", "200", "-1"
    })
    @ParameterizedTest
    void getEmailByUnknownUserIdTest(Long userId) {
        // GIVEN
        Optional<String> result;

        // WHEN
        result = repository.findEmailById(userId);

        // THEN
        assertThat(result).isEmpty();
    }

    @CsvSource({
            "John.doe@test.com",
            "john.doe@TEST.COM"
    })
    @ParameterizedTest
    void existsByEmailTest(String email) {
        // GIVEN
        boolean result;

        // WHEN
        result = repository.existsByEmailIgnoreCase(email);

        // THEN
        assertThat(result).isTrue();
    }

    @CsvSource({
            "john.doe@test.co",
            "johndoe@test.com"
    })
    @ParameterizedTest
    void existsByUnknownEmailTest(String unknownEmail) {
        // GIVEN
        boolean result;

        // WHEN
        result = repository.existsByEmailIgnoreCase(unknownEmail);

        // THEN
        assertThat(result).isFalse();
    }

}
