package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Async
    public CompletableFuture<User> createUser(User user) throws EntityException {
        boolean isEmailTaken = repository.existsByEmailIgnoreCase(user.getEmail());
        if(isEmailTaken) {
            return failedFuture(
                    new EntityException("User with the same email already exists")
            );
        }

        user.setId(null);
        user.setRole("USER");
        User hashedUser = hashUserPassword(user);
        User savedUser = repository.save(hashedUser);
        return completedFuture(savedUser);
    }

    @Async
    public CompletableFuture<List<User>> getUsers() {
        List<User> users = repository.findAll();
        return completedFuture(users);
    }

    @Async
    public CompletableFuture<User> getUserById(Long id) throws EntityNotFoundException {
        return repository.findById(id)
                .map(CompletableFuture::completedFuture)
                .orElse(failedFuture(
                        new EntityNotFoundException("User not found")
                ));
    }

    @Async
    public CompletableFuture<Long> getUserIdByEmail(String email) throws EntityNotFoundException {
        return repository.findIdByEmailIgnoreCase(email)
                .map(CompletableFuture::completedFuture)
                .orElse(
                        failedFuture(new EntityNotFoundException("No user found with the given email"))
                );
    }

    @Async
    public CompletableFuture<User> getUserByEmail(String email) throws EntityNotFoundException {
        return repository.findByEmail(email)
                .map(CompletableFuture::completedFuture)
                .orElse(failedFuture(
                        new EntityNotFoundException("User not found")
                ));
    }

    @Async
    public CompletableFuture<User> updateUserById(Long id, User user) throws EntityNotFoundException {
        if(user.getEmail() != null) {
            Optional<Long> userIdOfNewEmail = repository.findIdByEmailIgnoreCase(user.getEmail());
            if (userIdOfNewEmail.isPresent() && !userIdOfNewEmail.get().equals(id)) {
               return failedFuture(new EntityException("New email is already taken"));
            }
        }

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true);

        user.setId(null);
        User hashedUser = hashUserPassword(user);
        User savedUser = getUserById(id).join();
        mapper.map(hashedUser, savedUser);

        repository.save(savedUser);
        return completedFuture(savedUser);
    }

    @Async
    public CompletableFuture<Void> deleteUserById(Long id) throws EntityNotFoundException {
        boolean userExists = repository.existsById(id);
        if(!userExists) {
            return failedFuture(new EntityNotFoundException("User not found"));
        }

        repository.deleteById(id);
        return completedFuture(null);
    }

    public User hashUserPassword(User user) {
        User hashedUser = new User(user);
        String rawPassword = hashedUser.getPassword();
        if(rawPassword != null) {
            String hashedPassword = passwordEncoder.encode(rawPassword);
            hashedUser.setPassword(hashedPassword);
        }
        return hashedUser;
    }

}
