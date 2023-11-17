package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class UserServiceTests {

    private UserRepository repository;
    private UserService service;

    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void initTests() {
        this.repository = Mockito.mock(UserRepository.class);
        this.passwordEncoder = Mockito.mock(PasswordEncoder.class);
        this.service = new UserService(repository, passwordEncoder);

        testUser = new User("Jane Doe", "jane.doe@test.com", "myPassword1234", null);
        when(passwordEncoder.encode(any())).thenReturn("HASHED");
    }

    @Test
    void createUserTest() throws BadRequestException {
        // GIVEN
        User testUserWithId = new User(testUser);
        testUserWithId.setId(1L);
        User result;

        User expectedUser = new User(testUser);
        expectedUser.setPassword("HASHED");
        expectedUser.setRole("USER");

        when(repository.existsByEmailIgnoreCase(any()))
                .thenReturn(false);
        when(repository.save(any())).thenReturn(testUserWithId);
        // WHEN
        result = service.createUser(testUserWithId).join();

        // THEN
        verify(repository).existsByEmailIgnoreCase(testUser.getEmail());
        verify(passwordEncoder).encode(testUser.getPassword());
        verify(repository).save(expectedUser);
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
                .withCauseExactlyInstanceOf(BadRequestException.class);

        // THEN
        verify(repository).existsByEmailIgnoreCase(testUser.getEmail());
        verify(repository, never()).save(any());
    }

    @Test
    void getUsersTest() {
        // GIVEN
        List<User> userList = List.of(testUser);
        Pageable pageable = PageRequest.of(0, 50);

        Page<User> results;

        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(userList));
        // WHEN
        results = service.getUsers(null, pageable).join();

        // THEN
        verify(repository).findAll(pageable);
        assertThat(results).containsExactly(testUser);
    }

    @Test
    void getUsersWithSearchTest() {
        // GIVEN
        String nameSearch = "test search";
        List<User> userList = List.of(testUser);
        Pageable pageable = PageRequest.of(0, 50);

        Page<User> results;

        when(repository.findAllByDisplayNameContainingIgnoreCase(any(), any()))
                .thenReturn(new PageImpl<>(userList));
        // WHEN
        results = service.getUsers(nameSearch, pageable).join();

        // THEN
        verify(repository).findAllByDisplayNameContainingIgnoreCase(nameSearch, pageable);
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

        when(repository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(testUser));
        // WHEN
        result = service.getUserByEmail(userEmail, false).join();

        // THEN
        verify(repository).findByEmailIgnoreCase(userEmail);
        verify(repository, never()).findUserWithRelationsByEmail(userEmail);
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void getUserWithRelationsByEmailTest() throws EntityNotFoundException {
        // GIVEN
        String userEmail = "user@test.com";
        User result;

        when(repository.findUserWithRelationsByEmail(any()))
                .thenReturn(Optional.of(testUser));
        // WHEN
        result = service.getUserByEmail(userEmail, true).join();

        // THEN
        verify(repository).findUserWithRelationsByEmail(userEmail);
        verify(repository, never()).findByEmailIgnoreCase(userEmail);
        assertThat(result).isEqualTo(testUser);
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void getUnknownUserByEmail(boolean withRelations) {
        // GIVEN
        String unknownEmail = "unknown@test.com";

        when(repository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());
        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getUserByEmail(unknownEmail, withRelations).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getUserEmailByIdTest() {
        // GIVEN
        long userId = 1L;
        String email = "user@test.com";

        String result;

        when(repository.findEmailById(any()))
                .thenReturn(Optional.of(email));
        // WHEN
        result = service.getUserEmailById(userId).join();

        // THEN
        assertThat(result).isEqualTo(email);
        verify(repository).findEmailById(userId);
    }

    @Test
    void getUnknownUserEmailByIdTest() {
        // GIVEN
        long userId = 1L;

        when(repository.findEmailById(any()))
                .thenReturn(Optional.empty());
        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.getUserEmailById(userId).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        verify(repository).findEmailById(userId);
    }

    @Test
    void updateUserTest() {
        // GIVEN
        String newEmail = "new@test.com";

        User userUpdate = new User();
        userUpdate.setId(2L); // ID should be removed
        userUpdate.setEmail(newEmail);

        User currentUser = new User(testUser);
        currentUser.setId(1L);

        User updatedUser = new User(currentUser);
        updatedUser.setEmail(newEmail);

        User result;

        when(repository.save(any())).thenReturn(currentUser);
        when(repository.existsByEmailIgnoreCase(any())).thenReturn(false);
        // WHEN
        result = service.updateUser(currentUser, userUpdate).join();

        // THEN
        verify(repository).save(updatedUser);
        verify(repository).existsByEmailIgnoreCase(newEmail);
        assertThat(result).isEqualTo(updatedUser);
    }

    @Test
    void updateUserToTakenEmailTest() {
        // GIVEN
        String newEmail = "taken@test.com";

        User userUpdate = new User();
        userUpdate.setId(2L); // ID should be removed
        userUpdate.setEmail(newEmail);

        User currentUser = new User(testUser);
        currentUser.setId(1L);

        when(repository.save(any())).thenReturn(currentUser);
        when(repository.existsByEmailIgnoreCase(any())).thenReturn(true);
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateUser(currentUser, userUpdate).join())
                .withCauseExactlyInstanceOf(BadRequestException.class);

        // THEN
        verify(repository).existsByEmailIgnoreCase(newEmail);
        verify(repository, never()).save(any());
    }

    @Test
    void updateUserByIdTest() {
        // GIVEN
        Long userId = 1L;
        String newName = "New Name";

        User userUpdate = new User();
        userUpdate.setDisplayName(newName);

        User updatedUser = new User(testUser);
        updatedUser.setDisplayName(newName);

        User result;

        when(repository.findById(any()))
                .thenReturn(Optional.of(testUser));
        when(repository.save(any()))
                .thenReturn(updatedUser);
        // WHEN
        result = service.updateUserById(userId, userUpdate).join();

        // THEN
        verify(repository).findById(userId);
        verify(repository).save(updatedUser);

        assertThat(result).isEqualTo(updatedUser);
    }

    @Test
    void updateUnknownUserByIdTest() {
        // GIVEN
        Long userId = 1L;
        String newName = "New Name";

        User userUpdate = new User();
        userUpdate.setDisplayName(newName);

        when(repository.findById(any()))
                .thenReturn(Optional.empty());
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateUserById(userId, userUpdate).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        // THEN
        verify(repository).findById(userId);
        verify(repository, never()).save(any());
    }

    @Test
    void updateUserByEmailTest() {
        // GIVEN
        String userEmail = "user@test.com";
        String newName = "New Name";

        User userUpdate = new User();
        userUpdate.setDisplayName(newName);

        User updatedUser = new User(testUser);
        updatedUser.setDisplayName(newName);

        User result;

        when(repository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(testUser));
        when(repository.save(any()))
                .thenReturn(updatedUser);
        // WHEN
        result = service.updateUserByEmail(userEmail, userUpdate).join();

        // THEN
        verify(repository).findByEmailIgnoreCase(userEmail);
        verify(repository).save(updatedUser);

        assertThat(result).isEqualTo(updatedUser);
    }

    @Test
    void updateUnknownUserByEmailTest() {
        // GIVEN
        String userEmail = "user@test.com";
        String newName = "New Name";

        User userUpdate = new User();
        userUpdate.setDisplayName(newName);

        when(repository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());
        // WHEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.updateUserByEmail(userEmail, userUpdate).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        // THEN
        verify(repository).findByEmailIgnoreCase(userEmail);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteUserByIdTest() {
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
    void deleteUserByEmailTest() {
        // GIVEN
        String userEmail = "user@test.com";

        when(repository.existsByEmailIgnoreCase(any())).thenReturn(true);
        // WHEN
        service.deleteUserByEmail(userEmail).join();

        // THEN
        verify(repository).existsByEmailIgnoreCase(userEmail);
        verify(repository).deleteByEmailIgnoreCase(userEmail);
    }

    @Test
    void deleteUnknownUserByEmailTest() {
        // GIVEN
        String userEmail = "unknown@test.com";

        when(repository.existsByEmailIgnoreCase(any())).thenReturn(false);
        // WHEN
        // THEN
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> service.deleteUserByEmail(userEmail).join())
                .withCauseExactlyInstanceOf(EntityNotFoundException.class);

        verify(repository).existsByEmailIgnoreCase(userEmail);
        verify(repository, never()).deleteByEmailIgnoreCase(any());
    }

    @Test
    void hashUserPasswordTest() {
        // GIVEN
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        service = new UserService(repository, passwordEncoder);
        String rawPassword = testUser.getPassword();
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
