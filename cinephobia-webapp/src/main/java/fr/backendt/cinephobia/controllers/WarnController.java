package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.models.dto.WarnDTO;
import fr.backendt.cinephobia.services.WarnService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.CompletableFuture;

@Controller
public class WarnController {

    private final WarnService service;
    private final ModelMapper mapper;

    public WarnController(WarnService service) {
        this.service = service;
        this.mapper = new ModelMapper();
    }

    @GetMapping("/warn/{mediaType}/{mediaId}")
    public CompletableFuture<ModelAndView> getMediaWarns(@PathVariable("mediaType") String mediaTypeName, @PathVariable("mediaId") Long mediaId, @RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
        MediaType mediaType;
        try {
            mediaType = MediaType.valueOf(mediaTypeName);
        } catch(IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media type.");
        }

        Pageable pageable = Pageable.ofSize(page);
        CompletableFuture<Page<Warn>> warnPage = service.getWarnsForMedia(mediaId, mediaType, pageable);
        CompletableFuture<Page<WarnDTO>> warnDTOs = warnPage.thenApply(warns ->
                warns.map(warn -> mapper.map(warn, WarnDTO.class)));

        String mediaWarnsUri = "/warn/%s/%s".formatted(mediaType, mediaId);
        ModelAndView view =  new ModelAndView("fragments/warns :: warnList");
        return warnDTOs.thenApply(warns -> view
                .addObject("warnPage", warns)
                .addObject("warnsUri", mediaWarnsUri)
        );
    }

}
