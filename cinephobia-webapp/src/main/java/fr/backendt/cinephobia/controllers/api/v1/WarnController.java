package fr.backendt.cinephobia.controllers.api.v1;

import fr.backendt.cinephobia.mappers.WarnMapper;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.models.dto.WarnDTO;
import fr.backendt.cinephobia.services.WarnService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/warn")
public class WarnController {

    private final WarnService service;
    private final WarnMapper mapper;

    public WarnController(WarnService service, WarnMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public CompletableFuture<List<WarnDTO>> getWarns(@RequestParam(required = false) Long mediaId) {
        if(mediaId != null) {
            return service.getWarnsByMediaId(mediaId)
                    .thenApply(mapper::toDTOs);
        }
        return service.getAllWarns()
                .thenApply(mapper::toDTOs);
    }

    @GetMapping("/{id}")
    public CompletableFuture<WarnDTO> getWarn(@PathVariable Long id) {
        return service.getWarn(id).thenApply(mapper::toDTO);
    }

    @PostMapping
    public CompletableFuture<WarnDTO> createWarn(@Valid @RequestBody WarnDTO warnDto) {
        Warn warn = mapper.toEntity(warnDto);
        return service.createWarn(warn).thenApply(mapper::toDTO);
    }

}
