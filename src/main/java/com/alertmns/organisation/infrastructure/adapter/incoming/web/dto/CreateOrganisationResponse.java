package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Réponse de la création d'une organisation")
public record CreateOrganisationResponse(
        @Schema(description = "Identifiant de l'organisation créée", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id
) {}
