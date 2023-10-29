package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.dto.MediaDTO;
import fr.backendt.cinephobia.services.MediaService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.springframework.data.domain.Sort.Direction;

@Controller
public class MediaController {

    private static final Logger LOGGER = Logger.getLogger(MediaController.class);

    private final MediaService service;
    private final ModelMapper mapper;

    public MediaController(MediaService service) {
        this.service = service;
        this.mapper = new ModelMapper();
    }

    @GetMapping("/media")
    public String getMediaPage() {
        return "medias";
    }

    @GetMapping(value = "/media", headers = "Hx-Request")
    public CompletableFuture<ModelAndView> getMedias(@RequestParam(required = false) String search,
                                                     @RequestParam(defaultValue = "0", required = false) Integer page,
                                                     @RequestParam(defaultValue = "50", required = false) Integer size,
                                                     @RequestParam(defaultValue = "id", required = false) String sortBy,
                                                     @RequestParam(defaultValue = "desc", required = false) String order) {
        if(page < 0) page = 0;
        if(size < 1) size = 1;
        if(size > 500) size = 500;

        Direction direction = Direction.fromOptionalString(order).orElse(Direction.DESC);
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        CompletableFuture<Page<MediaDTO>> pageFuture = service.getMediaPage(search, pageable)
                .thenApply(mediaPage -> mediaPage.map(media -> mapper.map(media, MediaDTO.class)));

        return pageFuture
                .thenApply(mediaPage -> new ModelAndView("fragments :: mediaList")
                        .addObject("mediasPage", mediaPage))
                .exceptionally(exception -> {
                    LOGGER.error("Could not get media page.", exception.getCause());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not get medias.");
                });
    }

    @GetMapping("/media/{id}")
    public CompletableFuture<ModelAndView> getMedia(@PathVariable Long id) {
        return service.getMedia(id)
                .thenApply(media -> {
                    MediaDTO dto = mapper.map(media, MediaDTO.class);
                    return new ModelAndView("media").addObject("media", dto);
                })
                .exceptionally(exception -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found.");
                });
    }

    @DeleteMapping("/admin/media/{id}")
    public HtmxResponse deleteMedia(@PathVariable Long id) {
        return service.deleteMedia(id)
                .thenApply(future -> HtmxResponse.builder()
                        .redirect("/media")
                        .build())
                .exceptionally(exception -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
                }).join();
    }

    @GetMapping("/admin/media/{id}")
    public CompletableFuture<ModelAndView> getMediaEditForm(@PathVariable Long id) {
        ModelAndView model = new ModelAndView("fragments :: mediaForm");
        return service.getMedia(id)
                .thenApply(media -> {
                    MediaDTO dto = mapper.map(media, MediaDTO.class);
                    return model.addObject("media", dto);
                })
                .exceptionally(exception -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
                });
    }

    @PostMapping("/admin/media/{id}")
    public CompletableFuture<ModelAndView> updateMedia(@PathVariable Long id, @ModelAttribute("media") @Validated MediaDTO mediaDTO, BindingResult results) {
        if(results.hasErrors()) {
            ModelAndView model = new ModelAndView("fragments :: mediaForm")
                    .addObject("media", mediaDTO);
            return completedFuture(model);
        }

        Media mediaUpdate = mapper.map(mediaDTO, Media.class);
        return service.updateMedia(id, mediaUpdate)
                .thenApply(media -> {
                    MediaDTO savedMediaDto = mapper.map(media, MediaDTO.class);
                    return new ModelAndView("media :: media")
                            .addObject("media", savedMediaDto);
                })
                .exceptionally(exception -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
                });
    }

}
