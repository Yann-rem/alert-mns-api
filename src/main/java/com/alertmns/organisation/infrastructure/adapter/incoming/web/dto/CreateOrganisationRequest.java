package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de la création d'une organisation")
public record CreateOrganisationRequest(
        @Schema(description = "Nom de l'organisation", example = "Metz Numeric School")
        @NotBlank @Size(max = 150) String name
) {}
