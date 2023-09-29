package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;
import static java.util.concurrent.CompletableFuture.completedFuture;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Async
    public CompletableFuture<User> createUser(User user) {
        user.setId(null);
        try {
            User savedUser = repository.save(user);
            return completedFuture(savedUser);
        } catch(DataIntegrityViolationException exception) {
            throw new EntityException("User with the same email already exists");
        }
    }

    @Async
    public CompletableFuture<List<User>> getUsers() {
        List<User> users = repository.findAll();
        return completedFuture(users);
    }

    @Async
    public CompletableFuture<User> getUserById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return completedFuture(user);
    }

    @Async
    public CompletableFuture<User> getUserByEmail(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return completedFuture(user);
    }

    @Async
    public CompletableFuture<User> replaceUserById(Long id, User user) {
        boolean userExists = repository.existsById(id);
        if(!userExists) {
            throw new EntityNotFoundException("User not found");
        }

        user.setId(id);
        User savedUser = repository.save(user);
        return completedFuture(savedUser);
    }

    @Async
    public CompletableFuture<User> updateUserById(Long id, User user) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true);

        user.setId(null);
        User savedUser = getUserById(id).join();
        mapper.map(user, savedUser);

        repository.save(savedUser);
        return completedFuture(savedUser);
    }

    @Async
    public CompletableFuture<Void> deleteUserById(Long id) {
        boolean userExists = repository.existsById(id);
        if(!userExists) {
            throw new EntityNotFoundException("User not found");
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
