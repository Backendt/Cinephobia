package fr.backendt.cinephobia.mappers;

import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.models.dto.MediaDTO;
import fr.backendt.cinephobia.services.PlatformService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class MediaMapper {

    private final PlatformService platformService;

    public MediaMapper(PlatformService platformService) {
        this.platformService = platformService;
    }

    public Media toEntity(MediaDTO dto) {
        List<Platform> platforms = dto.platformsId().stream()
                .map(platformService::getPlatform)
                .map(CompletableFuture::join)
                .toList();

        return new Media(dto.id(), dto.title(), dto.imageUrl(), platforms);
    }

    public MediaDTO toDTO(Media media) {
        List<Long> platformsId = media.getPlatforms().stream()
                .map(Platform::getId)
                .toList();

        return new MediaDTO(
                media.getId(),
                media.getTitle(),
                media.getImageUrl(),
                platformsId
        );
    }

    public List<MediaDTO> toDTOs(List<Media> medias) {
        return medias.stream()
                .map(this::toDTO)
                .toList();
    }

}