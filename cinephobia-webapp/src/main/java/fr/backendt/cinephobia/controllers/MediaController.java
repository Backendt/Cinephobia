package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.dto.MediaDTO;
import fr.backendt.cinephobia.services.MediaService;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.CompletableFuture;

import static org.springframework.data.domain.Sort.Direction;

@Controller
@RequestMapping("/media")
public class MediaController {

    private static final Logger LOGGER = Logger.getLogger(MediaController.class);

    private final MediaService service;

    public MediaController(MediaService service) {
        this.service = service;
    }

    @GetMapping
    public String getMediaPage() {
        return "medias";
    }

    @GetMapping(headers = "Hx-Request")
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

        ModelMapper mapper = new ModelMapper();
        CompletableFuture<Page<MediaDTO>> pageFuture = service.getMediaPage(search, pageable)
                .thenApply(mediaPage -> mediaPage.map(media -> mapper.map(media, MediaDTO.class)));

        return pageFuture
                .thenApply(mediaPage -> new ModelAndView("fragments :: mediaList")
                        .addObject("mediasPage", mediaPage))
                .exceptionally(exception -> {
                    LOGGER.error("Could not get media page.", exception.getCause());
                    return new ModelAndView("error")
                            .addObject("err", "Could not get medias.");
                });
    }

}
