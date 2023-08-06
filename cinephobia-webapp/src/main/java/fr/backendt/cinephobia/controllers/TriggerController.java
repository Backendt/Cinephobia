package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.services.TriggerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/api/v1/trigger")
public class TriggerController {

    private final TriggerService service;

    @Autowired
    public TriggerController(TriggerService service) {
        this.service = service;
    }

    @PostMapping
    public CompletableFuture<Trigger> createTrigger(@Valid @RequestBody Trigger trigger) {
        return service.createTrigger(trigger);
    }

    @GetMapping
    public CompletableFuture<List<Trigger>> getTriggers(@RequestParam(required = false) String search) {
        if(search != null && !search.isBlank()) {
            return service.getTriggersContainingString(search);
        }
        return service.getAllTriggers();
    }

    @GetMapping("/{id}")
    public CompletableFuture<Trigger> getTrigger(@PathVariable Long id) {
        return service.getTrigger(id);
    }

}
