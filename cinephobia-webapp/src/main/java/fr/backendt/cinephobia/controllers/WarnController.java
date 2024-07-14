package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.models.dto.WarnDTO;
import fr.backendt.cinephobia.models.dto.WarnResponseDTO;
import fr.backendt.cinephobia.services.TriggerService;
import fr.backendt.cinephobia.services.UserService;
import fr.backendt.cinephobia.services.WarnService;
import jakarta.servlet.http.HttpServletResponse;
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
    private final UserService userService;
    private final ModelMapper mapper;

    public WarnController(WarnService service, TriggerService triggerService, UserService userService) {
        this.service = service;
        this.triggerService = triggerService;
        this.userService = userService;
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

        MediaType mediaType = MediaType.fromName(mediaTypeName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media type."));

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

    @GetMapping("/warn")
    public CompletableFuture<ModelAndView> getWarnCreationForm() {
        ModelAndView model = new ModelAndView("fragments/warns :: warnForm");
        model.addObject("warn", new WarnDTO());
        return completedFuture(model);
    }

    @PostMapping("/media/{mediaType}/{mediaId}")
    public CompletableFuture<ModelAndView> createWarn(@PathVariable("mediaType") String mediaTypeName, @PathVariable("mediaId") Long mediaId, @ModelAttribute("warn") @Validated WarnDTO warnDTO, BindingResult results, Authentication authentication, HttpServletResponse response) {
        ModelAndView errorModel = new ModelAndView("fragments/warns :: warnForm");
        if(results.hasErrors()) {
            response.addHeader("HX-Retarget", "#warnForm");
            response.addHeader("HX-Reswap", "outerHTML");
            return completedFuture(errorModel.addObject("warn", warnDTO));
        }

        String userEmail = authentication.getName();
        CompletableFuture<Warn> futureWarn = buildWarnFromDTO(mediaTypeName, mediaId, warnDTO, userEmail, results);

        return futureWarn.thenCompose(service::createWarn)
                .thenApply(savedWarn -> {
                    WarnResponseDTO savedDTO = mapper.map(savedWarn, WarnResponseDTO.class);
                    return new ModelAndView("fragments/warns :: warn")
                            .addObject("warn", savedDTO);
                }).exceptionally(exception -> {
                    if(!results.hasErrors()) {
                        results.rejectValue("triggerId", "warn-already-exist", "You already created a similar warn.");
                    }
                    response.addHeader("HX-Retarget", "#warnForm");
                    response.addHeader("HX-Reswap", "outerHTML");
                    return errorModel.addObject("warn", warnDTO);
                });
    }

    // TODO Add button on view to delete warns
    @DeleteMapping("/warn/{warnId}")
    public CompletableFuture<ResponseEntity<Void>> deleteWarn(@PathVariable Long warnId, Authentication authentication) {
        String userEmail = authentication.getName();
        return service.deleteWarnIfOwnedByUser(warnId, userEmail)
                .thenApply(future -> ResponseEntity.ok().build());
    }

    private CompletableFuture<Warn> buildWarnFromDTO(String mediaTypeName, long mediaId, WarnDTO warnDTO, String userEmail, BindingResult result) {
        Warn warn = mapper.map(warnDTO, Warn.class); // Convert DTO

        // Set media
        MediaType mediaType = MediaType.fromName(mediaTypeName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media type."));
        warn.setMediaType(mediaType);
        warn.setMediaId(mediaId);

        // Add relations
        CompletableFuture<Void> futureTrigger = triggerService.getTrigger(warnDTO.getTriggerId())
                .thenAccept(warn::setTrigger)
                .exceptionally(exception -> {
                    result.rejectValue("triggerId", "trigger-doesnt-exist", "Trigger does not exist");
                    throw new EntityNotFoundException("Trigger does not exist");
                });

        CompletableFuture<Void> futureUser = userService.getUserByEmail(userEmail, false)
                .thenAccept(warn::setUser);

        return CompletableFuture.allOf(futureUser, futureTrigger)
                .thenApply(futures -> warn);
    }

}
