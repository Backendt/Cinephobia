package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.dto.TriggerDTO;
import fr.backendt.cinephobia.services.TriggerService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Controller
public class TriggerController {

    private static final Logger LOGGER = Logger.getLogger(TriggerController.class);

    private final TriggerService service;
    private final ModelMapper mapper;

    public TriggerController(TriggerService service) {
        this.service = service;
        this.mapper = new ModelMapper();
    }

    @PostMapping("/admin/trigger")
    public CompletableFuture<ModelAndView> createTrigger(@Validated({Default.class, NotNull.class}) @ModelAttribute("trigger") TriggerDTO dto, BindingResult results) {
        ModelAndView errorModel = new ModelAndView("fragments/triggers :: triggerForm");
        if(results.hasErrors()) {
            return completedFuture(
                    errorModel.addObject("trigger", dto)
            );
        }
        Trigger trigger = mapper.map(dto, Trigger.class);

        return service.createTrigger(trigger)
                .thenApply(savedTrigger -> {
                    TriggerDTO savedDto = mapper.map(trigger, TriggerDTO.class);
                    return new ModelAndView("fragments/triggers :: trigger")
                            .addObject("trigger", savedDto);
                }).exceptionally(exception -> {
                    results.rejectValue("name", "already-exists", "Trigger already exists");
                    return errorModel.addObject("trigger", dto);
                });
    }

    @GetMapping("/admin/trigger")
    public CompletableFuture<ModelAndView> getTriggerCreationForm() {
        ModelAndView model = new ModelAndView("fragments/triggers :: triggerForm")
                .addObject("trigger", new TriggerDTO());
        return completedFuture(model);
    }

    @GetMapping("/trigger")
    public String getTriggersView() {
        return "triggers";
    }

    @GetMapping(value = "/trigger", headers = "Hx-Request")
    public CompletableFuture<ModelAndView> getTriggers(@RequestParam(required = false) String search,
                                                       @RequestParam(required = false, defaultValue = "0") Integer page,
                                                       @RequestParam(required = false, defaultValue = "50") Integer size) {
        if(page < 0) page = 0;
        if(size < 1) size = 1;
        else if(size > 300) size = 300;

        Pageable pageable = PageRequest.of(page, size);
        return service.getTriggers(search, pageable)
                .thenApply(triggers -> new ModelAndView("fragments/triggers :: triggerList")
                        .addObject("triggers", triggers))
                .exceptionally(exception -> {
                    LOGGER.error("Could not get triggers page.", exception.getCause());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not get triggers.");
                });
    }

    @GetMapping("/admin/trigger/{id}")
    public CompletableFuture<ModelAndView> getTriggerUpdateForm(@PathVariable Long id) {
        return service.getTrigger(id)
                .thenApply(trigger -> {
                    TriggerDTO triggerDto = mapper.map(trigger, TriggerDTO.class);
                    return new ModelAndView("fragments/triggers :: triggerForm")
                        .addObject("trigger", triggerDto);
            });
    }

    @PostMapping("/admin/trigger/{id}")
    public CompletableFuture<ModelAndView> updateTrigger(@PathVariable Long id, @Validated @ModelAttribute("trigger") TriggerDTO dto, BindingResult results) {
        ModelAndView errorModel = new ModelAndView("fragments/triggers :: triggerForm");
        if(results.hasErrors()) {
            return completedFuture(
                    errorModel.addObject("trigger", dto)
            );
        }
        Trigger trigger = mapper.map(dto, Trigger.class);

        return service.updateTrigger(id, trigger)
                .thenApply(savedTrigger -> {
                    TriggerDTO savedDto = mapper.map(savedTrigger, TriggerDTO.class);
                    return new ModelAndView("fragments/triggers :: trigger")
                            .addObject("trigger", savedDto);
                }).exceptionally(exception -> {
                    if(exception.getCause() instanceof EntityException.EntityNotFoundException notFoundException) {
                        throw notFoundException;
                    }
                    results.rejectValue("name", "already-exists", "Trigger already exists");
                    return errorModel.addObject("trigger", dto);
                });
    }

    @DeleteMapping("/admin/trigger/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteTrigger(@PathVariable Long id) {
        return service.deleteTrigger(id)
                .thenApply(future -> ResponseEntity.ok().build());
    }

}
