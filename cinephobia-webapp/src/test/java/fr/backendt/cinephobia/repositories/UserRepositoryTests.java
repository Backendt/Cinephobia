package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

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
                .hasNoNullFieldsOrProperties();
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
        List<User> results;

        // WHEN
        results = repository.findAll();

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0)).hasNoNullFieldsOrProperties();
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

    @Test
    void getUserByEmailTest() {
        // GIVEN
        String userEmail = "john.doe@test.com";
        Optional<User> result;

        // WHEN
        result = repository.findByEmail(userEmail);

        // THEN
        assertThat(result).isNotEmpty();
        assertThat(result.get()).hasNoNullFieldsOrProperties();
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

    @CsvSource({
            "john.doe@test.com",
            "John.doe@test.COM",
            "JOHN.DOE@TEST.COM"
    })
    @ParameterizedTest
    void getUserIdFromEmailTest(String email) {
        // GIVEN
        Optional<Long> result;

        long expectedId = 1L;
        // WHEN
        result = repository.findIdByEmailIgnoreCase(email);

        // THEN
        assertThat(result).contains(expectedId);
    }

    @Test
    void getUserIdFromUnknownEmailTest() {
        // GIVEN
        String unknownEmail = "john.doe@test.co";
        Optional<Long> result;

        // WHEN
        result = repository.findIdByEmailIgnoreCase(unknownEmail);

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
