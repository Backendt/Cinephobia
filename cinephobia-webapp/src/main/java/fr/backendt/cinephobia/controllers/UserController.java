package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.ProfileResponseDTO;
import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.models.dto.UserResponseDTO;
import fr.backendt.cinephobia.services.TriggerService;
import fr.backendt.cinephobia.services.UserService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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


@Controller
public class UserController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class);

    private final UserService service;
    private final TriggerService triggerService;
    private final SessionRegistry sessions;
    private final ModelMapper mapper;

    public UserController(UserService service, TriggerService triggerService, SessionRegistry sessions) {
        this.service = service;
        this.triggerService = triggerService;
        this.sessions = sessions;
        this.mapper = new ModelMapper();
    }

    @GetMapping("/admin/user")
    public String getUsersView() {
        return "admin/users";
    }

    @GetMapping(value = "/admin/user", headers = "Hx-Request")
    public CompletableFuture<ModelAndView> getUsers(@RequestParam(value = "search", required = false) String nameSearch,
                                                    @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                    @RequestParam(value = "size", required = false, defaultValue = "50") Integer size) {
        if(page < 0) page = 0;
        if(size < 1) size = 1;
        else if(size > 500) size = 500;

        Pageable pageable = PageRequest.of(page, size);

        return service.getUsers(nameSearch, pageable)
                .thenApply(users -> {
                    Page<UserResponseDTO> userDTOs = users.map(user -> mapper.map(user, UserResponseDTO.class));
                    return new ModelAndView("fragments/users :: userList").addObject("users", userDTOs);
                })
                .exceptionally(exception -> {
                    LOGGER.error("Could not get users", exception.getCause());
                    throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not get users");
                });
    }

    @GetMapping("/admin/user/{id}")
    public CompletableFuture<ModelAndView> getUserEditForm(@PathVariable Long id) {
        return service.getUserById(id)
                .thenApply(user -> {
                    UserResponseDTO userDTO = mapper.map(user, UserResponseDTO.class);
                    return new ModelAndView("fragments/users :: userForm")
                            .addObject("user", userDTO);
                });
    }

    @PostMapping("/admin/user/{id}")
    public CompletableFuture<ModelAndView> updateUser(@PathVariable Long id, @ModelAttribute("user") @Validated UserDTO userDTO, BindingResult result) {
        if(result.hasErrors()) {
            return completedFuture(
                    new ModelAndView("fragments/users :: userForm").addObject("user", userDTO)
            );
        }

        User userUpdate = mapper.map(userDTO, User.class);
        return service.updateUserById(id, userUpdate)
                .thenApply(user -> {
                    UserResponseDTO updatedUserDTO = mapper.map(user, UserResponseDTO.class);
                    return new ModelAndView("fragments/users :: user").addObject("user", updatedUserDTO);
                });
    }

    @PostMapping("/admin/user/role/{id}") // Not a GET request because it would make it vulnerable to CSRF
    public CompletableFuture<ModelAndView> makeUserAdmin(@PathVariable Long id) {
        User userUpdate = new User();
        userUpdate.setRole("ADMIN");
        return service.updateUserById(id, userUpdate)
                .thenApply(user -> {
                    UserResponseDTO updatedUserDTO = mapper.map(user, UserResponseDTO.class);
                    return new ModelAndView("fragments/users :: user").addObject("user", updatedUserDTO);
                });
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

    @GetMapping("/profile")
    public CompletableFuture<ModelAndView> getUserProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        return service.getUserByEmail(userEmail, true)
                .thenApply(user -> {
                    ProfileResponseDTO userDto = mapper.map(user, ProfileResponseDTO.class);
                    return new ModelAndView("profile").addObject("user", userDto);
                })
                .exceptionally(exception -> new ModelAndView("redirect:/login"));
    }

    @GetMapping(value = "/profile", headers = "Hx-Request")
    public CompletableFuture<ModelAndView> getUserProfileEditForm(Authentication authentication) {
        String userEmail = authentication.getName();
        return service.getUserByEmail(userEmail, false)
                .thenApply(user -> {
                    UserResponseDTO userDto = mapper.map(user, UserResponseDTO.class);
                    return new ModelAndView("fragments/users :: profileForm")
                            .addObject("user", userDto);
                })
                .exceptionally(exception -> new ModelAndView("redirect:/login"));
    }

    @PostMapping("/profile")
    public CompletableFuture<ModelAndView> updateUserProfile(@Validated @ModelAttribute("user") UserDTO userUpdate, BindingResult result, Authentication authentication) {
        ModelAndView errorTemplate = new ModelAndView("fragments/users :: profileForm");
        if(result.hasErrors()) {
            return completedFuture(
                    errorTemplate.addObject("user", userUpdate)
            );
        }

        String userEmail = authentication.getName();
        User userUpdateEntity = mapper.map(userUpdate, User.class);

        return service.updateUserByEmail(userEmail, userUpdateEntity)
                .thenApply(user -> {
                    UserResponseDTO userDto = mapper.map(user, UserResponseDTO.class);
                    return new ModelAndView("fragments/users :: profile")
                            .addObject("user", userDto);
                })
                .exceptionally(exception -> {
                    if(exception.getCause() instanceof EntityNotFoundException) {
                        return new ModelAndView("redirect:/login");
                    }

                    result.rejectValue("email", "email-taken", "Email already taken");
                    return errorTemplate.addObject("user", userUpdate);
                });
    }

    @PostMapping("/profile/trigger") // TODO Write tests
    public CompletableFuture<ResponseEntity<Void>> addTriggerToProfile(Authentication authentication, Long id) {
        String userEmail = authentication.getName();
        return triggerService.getTrigger(id)
                .thenCompose(trigger -> service.addTriggerToUser(userEmail, trigger))
                .thenApply(future -> ResponseEntity.ok(null));
    }

    @DeleteMapping("/profile/trigger/{id}") // TODO Write tests
    public CompletableFuture<ResponseEntity<Void>> removeTriggerFromProfile(Authentication authentication, @PathVariable("id") Long triggerId) {
        String userEmail = authentication.getName();
        return service.removeTriggerFromUser(userEmail, triggerId)
                .thenApply(future -> ResponseEntity.ok(null));
    }

    @DeleteMapping("/profile")
    public HtmxResponse deleteUser(@AuthenticationPrincipal UserDetails currentUser) {
        // Logout user sessions
        sessions.getAllSessions(currentUser, false)
                .forEach(SessionInformation::expireNow);

        return service.deleteUserByEmail(currentUser.getUsername())
                .thenApply(future -> HtmxResponse.builder()
                        .redirect("/")
                        .build())
                .join();
    }
}
