package fr.backendt.cinephobia.mappers;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.models.dto.WarnDTO;
import fr.backendt.cinephobia.services.TriggerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarnMapper {

    private final TriggerService triggerService;

    public WarnMapper(TriggerService triggerService) {
        this.triggerService = triggerService;
    }

    public Warn toEntity(WarnDTO dto) throws EntityException.EntityNotFoundException {
        Trigger trigger = triggerService.getTrigger(dto.triggerId()).join();
        return new Warn(dto.id(), trigger, dto.mediaId(), dto.expositionLevel());
    }

    public WarnDTO toDTO(Warn entity) {
        Long triggerId = entity.getTrigger().getId();
        return new WarnDTO(entity.getId(), entity.getMediaId(), triggerId, entity.getExpositionLevel());
    }

    public List<WarnDTO> toDTOs(List<Warn> warns) {
        return warns.stream()
                .map(this::toDTO)
                .toList();
    }

}
