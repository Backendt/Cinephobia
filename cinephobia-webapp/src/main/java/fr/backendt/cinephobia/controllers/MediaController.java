package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.services.MediaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService service;

    @Autowired
    public MediaController(MediaService service) {
        this.service = service;
    }

    @GetMapping
    public CompletableFuture<List<Media>> getMedias(@RequestParam(required = false) String search) {
        if(search != null && !search.isBlank()) {
            return service.getMediaContainingInTitle(search);
        }
        return service.getAllMedias();
    }

    @GetMapping("/{id}")
    public CompletableFuture<Media> getMedia(@PathVariable Long id) {
        return service.getMedia(id);
    }

    @PostMapping
    public CompletableFuture<Media> createMedia(@Valid @RequestBody Media media) {
        return service.createMedia(media);
    }

}
