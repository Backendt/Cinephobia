package fr.backendt.cinephobia.services;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<User> createUser(User user) throws BadRequestException {
        boolean isEmailTaken = repository.existsByEmailIgnoreCase(user.getEmail());
        if(isEmailTaken) {
            return failedFuture(
                    new BadRequestException("User with the same email already exists")
            );
        }

        user.setId(null);
        user.setRole("USER");
        User hashedUser = hashUserPassword(user);
        User savedUser = repository.save(hashedUser);
        return completedFuture(savedUser);
    }

    @Async
    public CompletableFuture<Page<User>> getUsers(@Nullable String nameSearch, Pageable pageable) {
        Page<User> users = nameSearch == null ?
                repository.findAll(pageable) :
                repository.findAllByDisplayNameContainingIgnoreCase(nameSearch, pageable);
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
    public CompletableFuture<String> getUserEmailById(Long id) {
        return repository.findEmailById(id)
                .map(CompletableFuture::completedFuture)
                .orElse(
                        failedFuture(new EntityNotFoundException("No user found with the given id"))
                );
    }

    @Async
    public CompletableFuture<User> getUserByEmail(String email, boolean withRelations) throws EntityNotFoundException {
        Optional<User> optionalUser = withRelations ?
                repository.findUserWithRelationsByEmail(email) :
                repository.findByEmailIgnoreCase(email)
                        .map(user -> { // Remove uninitialized triggers
                            user.setTriggers(Set.of());
                            return user;
                        });

        return optionalUser.map(CompletableFuture::completedFuture)
                .orElse(failedFuture(
                        new EntityNotFoundException("User not found")
                ));
    }

    @Async
    public CompletableFuture<User> updateUserById(Long id, User userUpdate) throws EntityNotFoundException {
        return repository.findById(id)
                .map(user -> updateUser(user, userUpdate))
                .orElse(failedFuture(new EntityNotFoundException("User not found")));
    }

    @Async
    public CompletableFuture<User> updateUserByEmail(String email, User userUpdate) {
        return repository.findByEmailIgnoreCase(email)
                .map(user -> updateUser(user, userUpdate))
                .orElse(failedFuture(new EntityNotFoundException("User not found")));
    }

    public CompletableFuture<User> updateUser(User user, User userUpdate) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true);

        userUpdate.setId(null);
        User hashedUserUpdate = hashUserPassword(userUpdate);

        boolean isChangingEmail = userUpdate.getEmail() != null && !userUpdate.getEmail().equals(user.getEmail());
        if(isChangingEmail) {
            boolean isEmailTaken = repository.existsByEmailIgnoreCase(userUpdate.getEmail());
            if(isEmailTaken) {
                return failedFuture(new BadRequestException("User with the same email already exists"));
            }
        }

        mapper.map(hashedUserUpdate, user);
        User savedUser = repository.save(user);
        return completedFuture(savedUser);
    }

    @Async
    public CompletableFuture<Void> addTriggerToUser(String userEmail, Trigger trigger) {
        Optional<User> userFuture = repository.findUserWithRelationsByEmail(userEmail);
        if(userFuture.isEmpty()) {
            return failedFuture(new EntityNotFoundException("User not found"));
        }
        User user = userFuture.get();
        Set<Trigger> triggers = user.getTriggers();

        boolean wasNotPresent = triggers.add(trigger);
        if(!wasNotPresent) {
            return failedFuture(new BadRequestException("User already have the requested trigger"));
        }

        repository.save(user);
        return completedFuture(null);
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

    @Async
    public CompletableFuture<Void> deleteUserByEmail(String email) throws EntityNotFoundException {
        boolean userExists = repository.existsByEmailIgnoreCase(email);
        if(!userExists) {
            return failedFuture(new EntityNotFoundException("User not found"));
        }

        repository.deleteByEmailIgnoreCase(email);
        return completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> removeTriggerFromUser(String userEmail, Long triggerId) {
        Optional<User> userFuture = repository.findUserWithRelationsByEmail(userEmail);
        if(userFuture.isEmpty()) {
            return failedFuture(new EntityNotFoundException("User not found"));
        }
        User user = userFuture.get();
        Set<Trigger> triggers = user.getTriggers();

        boolean containedTrigger = triggers.removeIf(trigger -> trigger.getId().equals(triggerId));
        if(!containedTrigger) {
            return failedFuture(new BadRequestException("User does not have the requested trigger"));
        }

        user.setTriggers(triggers);
        repository.save(user);
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
