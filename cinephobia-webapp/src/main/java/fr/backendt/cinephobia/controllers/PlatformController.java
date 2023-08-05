package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.services.PlatformService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/platform")
public class PlatformController {

    private final PlatformService service;

    @Autowired
    public PlatformController(PlatformService service) {
        this.service = service;
    }

    @GetMapping
    public CompletableFuture<List<Platform>> getPlatforms(@RequestParam(required = false) String search) {
        if(search != null && !search.isBlank()) {
            return service.getPlatformsContainingInName(search);
        }
        return service.getAllPlatforms();
    }

    @GetMapping("/{id}")
    public CompletableFuture<Platform> getPlatform(@PathVariable Long id) {
        return service.getPlatform(id);
    }

    @PostMapping
    public CompletableFuture<Platform> createPlatform(@Valid @RequestBody Platform platform) {
        return service.createPlatform(platform);
    }

}
