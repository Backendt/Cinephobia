package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.services.UserService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.concurrent.CompletionException;

@Controller
public class AuthenticationController {

    private final UserService service;
    private final ModelMapper mapper;

    public AuthenticationController(UserService service) {
        this.service = service;
        this.mapper = new ModelMapper();
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
    public String registerUser(@Validated({NotNull.class, Default.class}) @ModelAttribute("user") UserDTO dto, BindingResult result, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("user", dto);
            return "register";
        }

        User rawUser = mapper.map(dto, User.class);
        User user = service.hashUserPassword(rawUser);
        user.setRole("USER");

        try {
            service.createUser(user).join();
        } catch(CompletionException exception) {
            if(exception.getCause() instanceof EntityException) {
                model.addAttribute("user", dto);
                result.rejectValue("email", "email-taken", "Email already taken");
                return "register";
            }

            if(exception.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new EntityException("Could not create user. Try again later");
        }
        return "redirect:/login";
    }

}
