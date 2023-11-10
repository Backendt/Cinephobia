package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.dto.TriggerDTO;
import fr.backendt.cinephobia.services.TriggerService;
import jakarta.validation.Valid;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Controller
public class TriggerController { // TODO Write tests

    private static final Logger LOGGER = Logger.getLogger(TriggerController.class);

    private final TriggerService service;

    public TriggerController(TriggerService service) {
        this.service = service;
    }

    @PostMapping("/admin/trigger")
    public CompletableFuture<ModelAndView> createTrigger(@Valid TriggerDTO dto, BindingResult results) {
        ModelAndView errorModel = new ModelAndView("fragments/triggers :: triggerForm");
        if(results.hasErrors()) {
            return completedFuture(
                    errorModel.addObject("trigger", dto)
            );
        }
        ModelMapper mapper = new ModelMapper();
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
                .thenApply(trigger -> new ModelAndView("fragments/triggers :: triggerForm")
                        .addObject("trigger", trigger));
    }

    @PostMapping("/admin/trigger/{id}")
    public CompletableFuture<ModelAndView> updateTrigger(@PathVariable Long id, @Valid TriggerDTO dto, BindingResult results) {
        ModelAndView errorModel = new ModelAndView("fragments/triggers :: triggerForm");
        if(results.hasErrors()) {
            return completedFuture(
                    errorModel.addObject("trigger", dto)
            );
        }
        ModelMapper mapper = new ModelMapper();
        Trigger trigger = mapper.map(dto, Trigger.class);

        return service.updateTrigger(id, trigger)
                .thenApply(savedTrigger -> {
                    TriggerDTO savedDto = mapper.map(savedTrigger, TriggerDTO.class);
                    return new ModelAndView("fragments/triggers :: trigger")
                            .addObject("trigger", savedDto);
                }); // TODO Catch exceptions
    }

    @DeleteMapping("/admin/trigger/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteTrigger(@PathVariable Long id) {
        return service.deleteTrigger(id)
                .thenApply(future -> ResponseEntity.ok().build());
    }

}
