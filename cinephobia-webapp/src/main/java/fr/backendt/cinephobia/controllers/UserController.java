package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.FullUserDTO;
import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.services.UserService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Controller
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/admin/user")
    public CompletableFuture<ModelAndView> getUsers(@RequestParam(value = "email", required = false) String emailSearch) {
        ModelAndView template = new ModelAndView("admin/users");

        CompletableFuture<List<User>> userEntityList;
        if(emailSearch != null) {
            userEntityList = service.getUserByEmail(emailSearch)
                    .thenApply(List::of)
                    .exceptionally(exception -> List.of());
        } else {
            userEntityList = service.getUsers();
        }

        ModelMapper mapper = new ModelMapper();
        CompletableFuture<List<FullUserDTO>> userList = userEntityList
                .thenApplyAsync(users -> users.stream()
                        .map(user -> mapper.map(user, FullUserDTO.class))
                        .toList()
                );

        return userList.thenApply(users ->
                template.addObject("users", users));
    }

    @GetMapping("/admin/user/{id}")
    public CompletableFuture<ModelAndView> getUserProfile(@PathVariable Long id) {
        ModelAndView template = new ModelAndView("profile");
        ModelMapper mapper = new ModelMapper();

        return service.getUserById(id)
                .thenApply(user -> {
                    FullUserDTO userDTO = mapper.map(user, FullUserDTO.class);
                    template.addObject("user", userDTO);
                    return template;
                })
                .exceptionally(exception -> new ModelAndView("redirect:/admin/user")); // TODO Show 404 page
    }

    @PostMapping("/admin/user/{id}")
    public CompletableFuture<ModelAndView> updateUser(@PathVariable Long id, FullUserDTO userDTO, BindingResult results) {
        ModelAndView template = new ModelAndView("profile");
        if(results.hasErrors()) {
            template.addObject("user", userDTO);
        }

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
                    template.addObject("user", userDto);
                    return template;
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
        return service.getUserIdByEmail(userEmail)
                .thenCompose(userId -> service.updateUserById(userId, userUpdateEntity))
                .thenApply(user -> {
                    FullUserDTO userDto = mapper.map(user, FullUserDTO.class);
                    template.addObject("user", userDto);
                    return template;
                })
                .exceptionally(exception -> new ModelAndView("redirect:/login"));
    }

    @DeleteMapping("/profile")
    public HtmxResponse deleteUser(Authentication authentication) {
        String userEmail = authentication.getName();
        service.getUserIdByEmail(userEmail).thenCompose(service::deleteUserById);
        return new HtmxResponse().browserRedirect("/login");
    }

    @DeleteMapping("/admin/user/{id}")
    public HtmxResponse deleteUser(@PathVariable Long id) {
        service.deleteUserById(id);
        return new HtmxResponse()
                .browserRedirect("/admin/user")
                .browserRefresh(true);
    }
}
