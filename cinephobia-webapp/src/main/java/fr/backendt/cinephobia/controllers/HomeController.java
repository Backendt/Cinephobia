package fr.backendt.cinephobia.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<String> getHome(Authentication auth) {
        return ResponseEntity.ok("Welcome " + auth.getName());
    }

}
