package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.FullUserDTO;
import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.services.UserService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class UserController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class);

    private final UserService service;
    private final SessionRegistry sessions;
    private final ModelMapper mapper;

    public UserController(UserService service, SessionRegistry sessions) {
        this.service = service;
        this.sessions = sessions;
        this.mapper = new ModelMapper();
    }

    @GetMapping("/admin/user")
    public CompletableFuture<ModelAndView> getUsers(@RequestParam(value = "search", required = false) String nameSearch,
                                                    @RequestParam(value = "page", required = false, defaultValue = "0") Integer pageIndex,
                                                    @RequestParam(value = "size", required = false, defaultValue = "50") Integer pageSize) {
        if(pageSize < 1) pageSize = 1;
        if(pageSize > 500) pageSize = 500;
        if(pageIndex < 0) pageIndex = 0;

        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        return service.getUsers(nameSearch, pageable)
                .thenApply(users -> {
                    Page<FullUserDTO> userDTOs = users.map(user -> mapper.map(user, FullUserDTO.class));
                    return new ModelAndView("admin/users").addObject("usersPage", userDTOs);
                })
                .exceptionally(exception -> {
                    LOGGER.error("Could not get users", exception.getCause());
                    throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not get users");
                });
    }

    @GetMapping("/admin/user/{id}")
    public CompletableFuture<ModelAndView> getUserProfile(@PathVariable Long id) {
        ModelAndView template = new ModelAndView("profile");

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
    public HtmxResponse deleteUser(@AuthenticationPrincipal UserDetails currentUser) {
        // Logout user sessions
        sessions.getAllSessions(currentUser, false)
                .forEach(SessionInformation::expireNow);

        return service.getUserIdByEmail(currentUser.getUsername())
                .thenAccept(service::deleteUserById)
                .thenApply(future -> HtmxResponse.builder()
                        .redirect("/")
                        .build())
                .exceptionally(exception -> {
                    throw new ResponseStatusException(NOT_FOUND, "User not found");
                })
                .join();
    }

    @DeleteMapping("/admin/user/{id}")
    public HtmxResponse deleteUser(@PathVariable Long id) {
        service.getUserEmailById(id)
                .thenAccept(userEmail -> sessions.getAllPrincipals().stream()
                        .filter(principal -> ((UserDetails) principal).getUsername().equals(userEmail)) // Get the target user principal
                        .map(principal -> sessions.getAllSessions(principal, false)) // Should only return 1 sessions list
                        .forEach(userSessions -> userSessions.forEach(SessionInformation::expireNow)) // Expires all sessions
                );

        return service.deleteUserById(id)
                .thenApply(future -> HtmxResponse.builder()
                        .redirect("/admin/user")
                        .build())
                .join(); // HtmxResponse doesn't seem to work when passed as a future
    }
}
