package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.FullUserDTO;
import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.services.UserService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class UserController {

    private final UserService service;
    private final SessionRegistry sessions;

    public UserController(UserService service, SessionRegistry sessions) {
        this.service = service;
        this.sessions = sessions;
    }

    @GetMapping("/admin/user")
    public CompletableFuture<ModelAndView> getUsers(@RequestParam(value = "email", required = false) String emailSearch) {

        ModelMapper mapper = new ModelMapper();
        if(emailSearch != null) {
            return service.getUserByEmail(emailSearch)
                    .thenApply(user -> {
                        FullUserDTO dto = mapper.map(user, FullUserDTO.class);
                        return new ModelAndView("profile").addObject("user", dto);
                    })
                    .exceptionally(exception -> {throw new ResponseStatusException(NOT_FOUND, "User not found");});
        }

        CompletableFuture<List<User>> userEntityList = service.getUsers();
        CompletableFuture<List<FullUserDTO>> userList = userEntityList
                .thenApplyAsync(users -> users.stream()
                        .map(user -> mapper.map(user, FullUserDTO.class))
                        .toList()
                );

        return userList.thenApply(users ->
                new ModelAndView("admin/users").addObject("users", users));
    }

    @GetMapping("/admin/user/{id}")
    public CompletableFuture<ModelAndView> getUserProfile(@PathVariable Long id) {
        ModelAndView template = new ModelAndView("profile");
        ModelMapper mapper = new ModelMapper();

        return service.getUserById(id)
                .thenApply(user -> {
                    FullUserDTO userDTO = mapper.map(user, FullUserDTO.class);
                    return template.addObject("user", userDTO);
                })
                .exceptionally(exception -> {throw new ResponseStatusException(NOT_FOUND, "User not found");});
    }

    @PostMapping("/admin/user/{id}")
    public CompletableFuture<ModelAndView> updateUser(@PathVariable Long id, @ModelAttribute("user") FullUserDTO userDTO) {
        ModelAndView template = new ModelAndView("profile");
        ModelMapper mapper = new ModelMapper();

        User userUpdate = mapper.map(userDTO, User.class);
        CompletableFuture<User> updatedUser = service.updateUserById(id, userUpdate);

        return updatedUser
                .thenApply(user -> {
                    FullUserDTO updatedUserDTO = mapper.map(user, FullUserDTO.class);
                    template.addObject("user", updatedUserDTO);
                    template.setStatus(HttpStatusCode.valueOf(201)); // "Created" status code
                    return template;
                })
                .exceptionally(exception -> {
                    template.addObject("user", userDTO);
                    template.setStatus(HttpStatusCode.valueOf(400)); // "Bad Request" status code
                    return template;
                });
    }

    @GetMapping("/profile")
    public CompletableFuture<ModelAndView> getUserProfile(Authentication authentication) {
        String userEmail = authentication.getName();

        return service.getUserByEmail(userEmail)
                .thenApply(user -> {
                    ModelMapper mapper = new ModelMapper();
                    FullUserDTO userDto = mapper.map(user, FullUserDTO.class);

                    ModelAndView template = new ModelAndView("profile");
                    return template.addObject("user", userDto);
                })
                .exceptionally(exception -> new ModelAndView("redirect:/login"));
    }

    @PostMapping("/profile")
    public CompletableFuture<ModelAndView> updateUserProfile(@Validated @ModelAttribute("user") UserDTO userUpdate, BindingResult result, Authentication authentication) {
        ModelAndView template = new ModelAndView("profile");
        if(result.hasErrors()) {
            template.addObject("user", userUpdate);
            return completedFuture(template);
        }

        ModelMapper mapper = new ModelMapper();
        User userUpdateEntity = mapper.map(userUpdate, User.class);

        String userEmail = authentication.getName();
        CompletableFuture<Long> userIdFuture = service.getUserIdByEmail(userEmail);
        if(userIdFuture.isCompletedExceptionally()) {
            return completedFuture(new ModelAndView("redirect:/login"));
        }

        return userIdFuture.thenCompose(userId -> service.updateUserById(userId, userUpdateEntity))
                .thenApply(user -> {
                    FullUserDTO userDto = mapper.map(user, FullUserDTO.class);
                    return template.addObject("user", userDto);
                })
                .exceptionally(exception -> {
                    result.rejectValue("email", "email-taken", "Email already taken");
                    return template.addObject("user", userUpdate);
                });
    }

    @DeleteMapping("/profile")
    public CompletableFuture<Void> deleteUser(@AuthenticationPrincipal UserDetails currentUser) {
        // Logout user sessions
        sessions.getAllSessions(currentUser, false)
                .forEach(SessionInformation::expireNow);

        return service.getUserIdByEmail(currentUser.getUsername())
                .thenAccept(service::deleteUserById)
                .exceptionally(exception -> {
                    throw new ResponseStatusException(NOT_FOUND, "User not found");
                });
    }

    @DeleteMapping("/admin/user/{id}")
    public HtmxResponse deleteUser(@PathVariable Long id) { // TODO Log out users
        service.deleteUserById(id);
        return new HtmxResponse()
                .browserRedirect("/admin/user")
                .browserRefresh(true);
    }
}