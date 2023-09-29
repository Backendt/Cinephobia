package fr.backendt.cinephobia.controllers.api.v1;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.mappers.UserResponseMapper;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.models.dto.UserResponseDTO;
import fr.backendt.cinephobia.services.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NamingConventions;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService service;
    private final ModelMapper mapper;
    private final UserResponseMapper responseMapper;

    public UserController(UserService service) {
        this.service = service;
        this.mapper = new ModelMapper();
        mapper.getConfiguration().setSourceNamingConvention(NamingConventions.NONE); // Java Records getters are x() instead of getX()

        this.responseMapper = new UserResponseMapper();
    }

    /*
    ADMIN ONLY
     */

    @RolesAllowed("ADMIN")
    @GetMapping("/{id}")
    public CompletableFuture<UserResponseDTO> getUser(@PathVariable Long id) {
        return service.getUserById(id)
                .thenApply(responseMapper);
    }

    @RolesAllowed("ADMIN")
    @PatchMapping("/{id}")
    public CompletableFuture<UserResponseDTO> updateUser(@PathVariable Long id, @Validated @RequestBody UserDTO userDtoPatch) {
        User rawUserPatch = mapper.map(userDtoPatch, User.class);
        User userPatch = service.hashUserPassword(rawUserPatch);
        return service.updateUserById(id, userPatch)
                .thenApply(responseMapper);
    }

    @RolesAllowed("ADMIN")
    @PutMapping("/{id}")
    public CompletableFuture<UserResponseDTO> replaceUser(@PathVariable Long id, @Validated({NotNull.class, Default.class}) @RequestBody UserDTO userDto) {
        User receivedUser = mapper.map(userDto, User.class);
        receivedUser.setRole("USER");
        User user = service.hashUserPassword(receivedUser);
        return service.replaceUserById(id, user)
                .thenApply(responseMapper);
    }

    @RolesAllowed("ADMIN")
    @DeleteMapping("/{id}")
    public CompletableFuture<Void> deleteUser(@PathVariable Long id) {
        return service.deleteUserById(id);
    }

    /*
    ALLOWED TO ANY USER
     */

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CompletableFuture<UserResponseDTO> createUser(@Validated({NotNull.class, Default.class}) @RequestBody UserDTO userDto) {
        User receivedUser = mapper.map(userDto, User.class);
        receivedUser.setRole("USER");
        User user = service.hashUserPassword(receivedUser);
        return service.createUser(user)
                .thenApply(responseMapper);
    }

    @GetMapping
    public CompletableFuture<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails currentUser) {
        Long userId = getUserId(currentUser);
        return getUser(userId);
    }

    @PatchMapping
    public CompletableFuture<UserResponseDTO> updateCurrentUser(@AuthenticationPrincipal UserDetails currentUser, @Validated @RequestBody UserDTO userUpdate) {
        Long userId = getUserId(currentUser);
        return updateUser(userId, userUpdate);
    }

    @PutMapping
    public CompletableFuture<UserResponseDTO> replaceCurrentUser(@AuthenticationPrincipal UserDetails currentUser, @Validated({NotNull.class, Default.class}) @RequestBody UserDTO userReplacement) {
        Long userId = getUserId(currentUser);
        return replaceUser(userId, userReplacement);
    }

    @DeleteMapping
    public CompletableFuture<Void> deleteCurrentUser(@AuthenticationPrincipal UserDetails currentUser) {
        Long userId = getUserId(currentUser);
        return deleteUser(userId);
    }

    private Long getUserId(UserDetails userDetails) {
        String userIdString = userDetails.getUsername();
        try {
            return Long.parseLong(userIdString);
        } catch(NumberFormatException exception) {
            throw new EntityException.EntityNotFoundException("Could not get current user.");
        }
    }

}
