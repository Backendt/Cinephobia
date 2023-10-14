package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.mappers.MediaMapper;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.dto.MediaDTO;
import fr.backendt.cinephobia.services.MediaService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.CompletableFuture;

import static org.springframework.data.domain.Sort.Direction;
import static java.util.concurrent.CompletableFuture.completedFuture;

@Controller
@RequestMapping("/media")
public class MediaController { // TODO Add tests

    private final MediaService service;
    private final MediaMapper mapper;
    public MediaController(MediaService service, MediaMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public String getMediaPage(Model model) {
        MediaDTO media = new MediaDTO(null, null, null, null);
        model.addAttribute("media", media);
        return "medias";
    }

    @PostMapping
    public CompletableFuture<ModelAndView> addMedia(@Valid @ModelAttribute("media") MediaDTO mediaDTO, BindingResult results) {
        ModelAndView model = new ModelAndView("fragments :: media");
        if(results.hasErrors()) {
            model.addObject("media", mediaDTO);
            return completedFuture(model);
        }

        Media media = mapper.toEntity(mediaDTO);
        return service.createMedia(media) // TODO Catch exceptions
                .thenApply(mapper::toDTO)
                .thenApply(savedDto -> {
                    model.addObject("media", savedDto);
                    return model;
                });
    }

    @HxRequest
    @GetMapping
    public CompletableFuture<ModelAndView> getMedias(@RequestParam(required = false) String search,
                                                     @RequestParam(defaultValue = "0", required = false) Integer page,
                                                     @RequestParam(defaultValue = "50", required = false) Integer size,
                                                     @RequestParam(defaultValue = "id", required = false) String sortBy,
                                                     @RequestParam(required = false) String order) {
        Sort sort = Sort.unsorted();
        if(sortBy != null && !sortBy.isBlank()) {
            Direction direction = Direction.fromOptionalString(order)
                    .orElse(Direction.DESC);
            sort = Sort.by(direction, sortBy);
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        CompletableFuture<Page<MediaDTO>> pageFuture = service.getMediaPage(search, pageable)
                .thenApply(mediaPage -> mediaPage.map(mapper::toDTO));

        ModelAndView model = new ModelAndView("fragments :: mediaList");
        return pageFuture.thenApply(mediaPage -> { // TODO Catch exceptions
                    model.addObject("numberOfPages", mediaPage.getTotalPages());
                    model.addObject("medias", mediaPage.getContent());
                    return model;
                }
        );
    }

}
