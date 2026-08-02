package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Détail d'une organisation")
public record OrganisationResponse(
        @Schema(description = "Identifiant de l'organisation") UUID organisationId,
        @Schema(description = "Nom de l'organisation", example = "Metz Numeric School") String name
) {}
