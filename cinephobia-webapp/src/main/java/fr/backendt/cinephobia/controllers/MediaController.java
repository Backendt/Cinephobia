package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.mappers.MediaMapper;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.dto.MediaDTO;
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
    private final MediaMapper mapper;

    @Autowired
    public MediaController(MediaService service, MediaMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public CompletableFuture<List<MediaDTO>> getMedias(@RequestParam(required = false) String search) {
        if(search != null && !search.isBlank()) {
            return service.getMediaContainingInTitle(search)
                    .thenApply(mapper::toDTOs);
        }
        return service.getAllMedias()
                .thenApply(mapper::toDTOs);
    }

    @GetMapping("/{id}")
    public CompletableFuture<MediaDTO> getMedia(@PathVariable Long id) {
        return service.getMedia(id).thenApply(mapper::toDTO);
    }

    @PostMapping
    public CompletableFuture<MediaDTO> createMedia(@Valid @RequestBody MediaDTO dto) {
        Media media = mapper.toEntity(dto);
        return service.createMedia(media).thenApply(mapper::toDTO);
    }

}
