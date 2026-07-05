package com.alertmns.alerting.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Réponse à la diffusion d'une alerte")
public record BroadcastAlertResponse(
        @Schema(description = "Identifiant de l'alerte diffusée")
        UUID alertId
) {}
