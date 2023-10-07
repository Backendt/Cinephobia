package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;
import static org.mockito.Mockito.*;

class UserServiceTests {

    private UserRepository repository;
    private UserService service;

    private User testUser;

    @BeforeEach
    void initTests() {
        this.repository = Mockito.mock(UserRepository.class);
        this.service = new UserService(repository, new BCryptPasswordEncoder());

        testUser = new User("Jane Doe", "jane.doe@test.com", "myPassword1234", "USER");
    }

    @Test
    void createUserTest() throws EntityException {
        // GIVEN
        User testUserWithId = new User(testUser);
        testUserWithId.setId(1L);
        User result;

        when(repository.existsByEmailIgnoreCase(any()))
                .thenReturn(false);
        when(repository.save(any())).thenReturn(testUserWithId);
        // WHEN
        result = service.createUser(testUserWithId).join();

        // THEN
        verify(repository).existsByEmailIgnoreCase(testUser.getEmail());
        verify(repository).save(testUser);
        assertThat(result).isNotNull();
    }

    @Test
    void createDuplicatedUserTest() {
        // GIVEN
        when(repository.existsByEmailIgnoreCase(any()))
                .thenReturn(true);

        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.createUser(testUser).join())
                .withCauseExactlyInstanceOf(EntityException.class);

        // THEN
        verify(repository).existsByEmailIgnoreCase(testUser.getEmail());
        verify(repository, never()).save(any());
    }

    @Test
    void getUsersTest() {
        // GIVEN
        List<User> results;

        when(repository.findAll()).thenReturn(List.of(testUser));
        // WHEN
        results = service.getUsers().join();

        // THEN
        verify(repository).findAll();
        assertThat(results).containsExactly(testUser);
    }

    @Test
    void getUserByIdTest() throws EntityNotFoundException {
        // GIVEN
        Long userId = 1L;
        User result;

        when(repository.findById(any()))
                .thenReturn(Optional.of(testUser));
        // WHEN
        result = service.getUserById(userId).join();

        // THEN
        verify(repository).findById(userId);
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void getUnknownUserByIdTest() {
        // GIVEN
        Long unknownUserId = 1L;

        when(repository.findById(any()))
                .thenReturn(Optional.empty());
        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getUserById(unknownUserId).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getUserByEmailTest() throws EntityNotFoundException {
        // GIVEN
        String userEmail = "user@test.com";
        User result;

        when(repository.findByEmail(any()))
                .thenReturn(Optional.of(testUser));
        // WHEN
        result = service.getUserByEmail(userEmail).join();

        // THEN
        verify(repository).findByEmail(userEmail);
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void getUnknownUserByEmail() {
        // GIVEN
        String unknownEmail = "unknown@test.com";

        when(repository.findByEmail(any()))
                .thenReturn(Optional.empty());
        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getUserByEmail(unknownEmail).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateUserByIdTest() throws EntityNotFoundException {
        // GIVEN
        Long userId = 1L;
        User userUpdate = new User();

        String newEmail = "new@test.com";
        userUpdate.setEmail(newEmail);

        User updatedUser = new User(testUser);
        updatedUser.setEmail(newEmail);

        User result;

        when(repository.findIdByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(userId));
        when(repository.findById(any()))
                .thenReturn(Optional.of(testUser)); // Used in getUser(id)
        when(repository.save(any()))
                .thenReturn(updatedUser);
        // WHEN
        result = service.updateUserById(userId, userUpdate).join();

        // THEN
        verify(repository).findIdByEmailIgnoreCase(newEmail);
        verify(repository).findById(userId); // Used in getUser(id)
        verify(repository).save(updatedUser);

        assertThat(result).isEqualTo(updatedUser);
    }

    @Test
    void updateUserByIdDoesntUpdateIdTest() throws EntityNotFoundException {
        // GIVEN
        Long userId = 1L;

        User userUpdate = new User();
        Long newId = 2L;
        userUpdate.setId(newId);
        String newEmail = "new@test.com";
        userUpdate.setEmail(newEmail);

        User updatedUser = new User(testUser);
        updatedUser.setEmail(newEmail);

        User result;

        when(repository.findIdByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(userId));
        when(repository.findById(any()))
                .thenReturn(Optional.of(testUser));
        when(repository.save(any()))
                .thenReturn(updatedUser);
        // WHEN
        result = service.updateUserById(userId, userUpdate).join();

        // THEN
        verify(repository).findIdByEmailIgnoreCase(testUser.getEmail());
        verify(repository).findById(userId);
        verify(repository).save(updatedUser);

        assertThat(result).isEqualTo(updatedUser);
    }

    @Test
    void updateUnknownUserByIdTest() {
        // GIVEN
        Long unknownUserId = 1L;

        User userUpdate = new User();
        userUpdate.setDisplayName("Not Found");

        when(repository.findById(any()))
                .thenReturn(Optional.empty());
        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateUserById(unknownUserId, userUpdate).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        verify(repository, never()).findIdByEmailIgnoreCase(any());
        verify(repository).findById(unknownUserId);
        verify(repository, never()).save(any());
    }

    @Test
    void updateUserByIdToUsedEmailTest() {
        // GIVEN
        long userId = 1L;
        long otherUserId = 2L;
        String takenEmail = "taken@email.com";

        User userUpdate = new User();
        userUpdate.setEmail(takenEmail);

        when(repository.findIdByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(otherUserId));
        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateUserById(userId, userUpdate).join())
                .withCauseExactlyInstanceOf(EntityException.class);

        verify(repository).findIdByEmailIgnoreCase(takenEmail);
        verify(repository, never()).save(any());
    }

    @Test
    void replaceUserByIdTest() throws EntityNotFoundException {
        // GIVEN
        Long userId = 1L;
        User newUser = new User("New User", "user@test.com", "newpassword", "USER");
        User expectedUser = new User(newUser);
        expectedUser.setId(userId);

        User result;

        when(repository.existsById(any())).thenReturn(true);
        when(repository.findIdByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(expectedUser);
        // WHEN
        result = service.replaceUserById(userId, newUser).join();

        // THEN
        verify(repository).existsById(userId);
        verify(repository).findIdByEmailIgnoreCase(newUser.getEmail());
        verify(repository).save(expectedUser);
        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    void replaceUnknownUserByIdTest() {
        // GIVEN
        Long userId = 1L;

        when(repository.existsById(any())).thenReturn(false);
        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.replaceUserById(userId, testUser).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);
        verify(repository).existsById(userId);
        verify(repository, never()).save(any());
    }

    @Test
    void replaceUserByIdToTakenEmailTest() {
        // GIVEN
        long userId = 1L;
        long otherUserId = 2L;

        when(repository.existsById(any())).thenReturn(true);
        when(repository.findIdByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(otherUserId));
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.replaceUserById(userId, testUser).join())
                .withCauseExactlyInstanceOf(EntityException.class);
        // THEN
        verify(repository).findIdByEmailIgnoreCase(testUser.getEmail());
        verify(repository).existsById(userId);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteUserByIdTest() throws EntityNotFoundException {
        // GIVEN
        Long userId = 1L;

        when(repository.existsById(any())).thenReturn(true);
        // WHEN
        service.deleteUserById(userId).join();

        // THEN
        verify(repository).existsById(userId);
        verify(repository).deleteById(userId);
    }

    @Test
    void deleteUnknownUserByIdTest() {
        // GIVEN
        Long userId = 1L;

        when(repository.existsById(any())).thenReturn(false);
        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.deleteUserById(userId).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        verify(repository).existsById(userId);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void hashUserPasswordTest() {
        // GIVEN
        String rawPassword = testUser.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String resultPassword;

        // WHEN
        resultPassword = service.hashUserPassword(testUser).getPassword();

        // THEN
        assertThat(resultPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, resultPassword)).isTrue();
    }

    @Test
    void hashUserNullPasswordTest() {
        // GIVEN
        testUser.setPassword(null);
        String resultPassword;

        // WHEN
        resultPassword = service.hashUserPassword(testUser).getPassword();

        // THEN
        assertThat(resultPassword).isNull();
    }

}
