package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.services.UserService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
public class AuthenticationController {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationController.class);

    private final UserService service;

    public AuthenticationController(UserService service) {
        this.service = service;
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String getRegistrationPage(Model model) {
        UserDTO user = new UserDTO(null, null, null);
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register")
    public CompletableFuture<ModelAndView> registerUser(@Validated({NotNull.class, Default.class}) @ModelAttribute("user") UserDTO dto, BindingResult result) {
        ModelAndView model = new ModelAndView("register");
        model.addObject("user", dto);

        if(result.hasErrors()) {
            return completedFuture(model);
        }

        ModelMapper mapper = new ModelMapper();
        User user = mapper.map(dto, User.class);

        return service.createUser(user)
                .thenApply(future -> new ModelAndView("redirect:/login"))
                .exceptionally(exception -> {
                    if(exception.getCause() instanceof BadRequestException) {
                        result.rejectValue("email", "email-taken", "Email already taken");
                        return model;
                    }

                    LOGGER.error("Could not create user", exception.getCause());
                    throw new ResponseStatusException(BAD_REQUEST, "Could not create user");
                });
    }

}
