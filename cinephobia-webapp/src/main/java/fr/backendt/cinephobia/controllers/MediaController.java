package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.services.MediaService;
import org.jboss.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.CompletableFuture;

@Controller
public class MediaController {

    private static final Logger LOGGER = Logger.getLogger(MediaController.class);

    private final MediaService service;

    public MediaController(MediaService service) {
        this.service = service;
    }

    @GetMapping("/media")
    public String getMediaPage() {
        return "medias";
    }

    @GetMapping(value = "/media", headers = "Hx-Request")
    public CompletableFuture<ModelAndView> getMedias(@RequestParam(required = false) String search, @RequestParam(defaultValue = "1", required = false) Integer page) {
        if(page < 1) page = 1;

        return service.getMedias(search, page)
                .thenApply(searchResults -> new ModelAndView("fragments/medias :: mediaList")
                        .addObject("mediasPage", searchResults))
                .exceptionally(exception -> {
                    LOGGER.error("Could not get media page.", exception.getCause());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not get medias.");
                });
    }

    @GetMapping("/media/{type}/{id}")
    public CompletableFuture<ModelAndView> getMedia(@PathVariable("type") String typeString, @PathVariable("id") Long id) {
        CompletableFuture<Media> mediaFuture = typeString.equalsIgnoreCase(MediaType.MOVIE.name()) ?
                service.getMovie(id) :
                service.getSeries(id);

        return mediaFuture
                .thenApply(media -> new ModelAndView("media")
                        .addObject("media", media))
                .exceptionally(exception -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found.");
                });
    }

}
