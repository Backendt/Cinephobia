package fr.backendt.cinephobia.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record MediaDTO (

    Long id,

    @NotBlank(message = "The title is required")
    @Size(min = 2, max = 30, message = "The title must be between 2 and 30 characters")
    String title,

    @URL(protocol = "https", message = "The image url must be a valid https url")
    @NotBlank(message = "The image url is required")
    String imageUrl,

    @Size(max = 10, message = "There must be less than 10 platforms")
    List<Long> platformsId
) {}
