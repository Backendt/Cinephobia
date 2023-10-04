package fr.backendt.cinephobia.mappers;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.models.dto.WarnDTO;
import fr.backendt.cinephobia.services.MediaService;
import fr.backendt.cinephobia.services.TriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarnMapper {

    private final MediaService mediaService;
    private final TriggerService triggerService;

    @Autowired
    public WarnMapper(MediaService mediaService, TriggerService triggerService) {
        this.mediaService = mediaService;
        this.triggerService = triggerService;
    }

    public Warn toEntity(WarnDTO dto) throws EntityException.EntityNotFoundException {
        Media media = mediaService.getMedia(dto.mediaId()).join();
        Trigger trigger = triggerService.getTrigger(dto.triggerId()).join();

        return new Warn(dto.id(), trigger, media, dto.expositionLevel());
    }

    public WarnDTO toDTO(Warn entity) {
        Long triggerId = entity.getTrigger().getId();
        Long mediaId = entity.getMedia().getId();
        return new WarnDTO(entity.getId(), mediaId, triggerId, entity.getExpositionLevel());
    }

    public List<WarnDTO> toDTOs(List<Warn> warns) {
        return warns.stream()
                .map(this::toDTO)
                .toList();
    }

}
