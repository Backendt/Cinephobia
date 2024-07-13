package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.models.dto.WarnDTO;
import fr.backendt.cinephobia.models.dto.WarnResponseDTO;
import fr.backendt.cinephobia.services.TriggerService;
import fr.backendt.cinephobia.services.WarnService;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Controller
public class WarnController {

    private static final Logger LOGGER = Logger.getLogger(WarnController.class);

    private final WarnService service;
    private final TriggerService triggerService;
    private final ModelMapper mapper;

    public WarnController(WarnService service, TriggerService triggerService) {
        this.service = service;
        this.triggerService = triggerService;
        this.mapper = new ModelMapper();
    }

    @GetMapping("/warn/{mediaType}/{mediaId}")
    public CompletableFuture<ModelAndView> getMediaWarns(@PathVariable("mediaType") String mediaTypeName,
                                                         @PathVariable("mediaId") Long mediaId,
                                                         @RequestParam(required = false, value = "page", defaultValue = "0") Integer page,
                                                         @RequestParam(required = false, value = "size", defaultValue = "50") Integer size) {
        if(page < 0) page = 0;
        if(size < 1) size = 1;
        else if(size > 300) size = 300;

        MediaType mediaType;
        try {
            mediaType = MediaType.valueOf(mediaTypeName);
        } catch(IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media type.");
        }

        Pageable pageable = PageRequest.of(page, size);
        CompletableFuture<Page<Warn>> warnPage = service.getWarnsForMedia(mediaId, mediaType, pageable);
        CompletableFuture<Page<WarnResponseDTO>> warnDTOs = warnPage.thenApply(warns ->
                warns.map(warn -> mapper.map(warn, WarnResponseDTO.class)));

        String mediaWarnsUri = "/warn/%s/%s".formatted(mediaType, mediaId);
        ModelAndView view =  new ModelAndView("fragments/warns :: warnList");
        return warnDTOs.thenApply(warns -> view
                .addObject("warnPage", warns)
                .addObject("warnsUri", mediaWarnsUri)
        ).exceptionally(exception -> {
            LOGGER.error("Could not get warns page.", exception.getCause());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not get warns.");
        });
    }

    @PostMapping("/warn/{mediaType}/{mediaId}") // TODO Write tests
    public CompletableFuture<ModelAndView> createMediaWarn(@PathVariable("mediaType") String mediaTypeName, @PathVariable("mediaId") Long mediaId, @Validated WarnDTO warnDTO, BindingResult results) {
        ModelAndView errorModel = new ModelAndView("fragments/warns :: warnForm");
        if(results.hasErrors()) {
            return completedFuture(
                    errorModel.addObject("warn", warnDTO)
            );
        }

        boolean triggerExists = triggerService.doesTriggerExists(warnDTO.getTriggerId());
        if(!triggerExists) {
            results.rejectValue("triggerId", "trigger-doesnt-exist", "Trigger does not exist.");
            return completedFuture(
                    errorModel.addObject("warn", warnDTO)
            );
        }

        Warn warn = mapper.map(warnDTO, Warn.class);
        return service.createWarn(warn)
                .thenApply(savedWarn -> {
                    WarnDTO savedDTO = mapper.map(savedWarn, WarnDTO.class);
                    return new ModelAndView("fragments/warns :: warn")
                            .addObject("warn", savedDTO);
                }).exceptionally(exception -> {
                    results.rejectValue("triggerId", "already-exists", "Warn already exists");
                    return errorModel.addObject("warn", warnDTO);
                });
    }

    @DeleteMapping("/warn/{warnId}") // TODO Write tests
    public CompletableFuture<ResponseEntity<Void>> deleteTrigger(@PathVariable Long warnId, Authentication authentication) {
        String userEmail = authentication.getName();
        return service.deleteWarnIfOwnedByUser(warnId, userEmail)
                .thenApply(future -> ResponseEntity.ok().build());
    }

}
