package com.alertmns.alerting.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de diffusion d'une alerte")
public record BroadcastAlertRequest(
        @Schema(description = "Contenu de l'alerte", example = "Fermeture exceptionnelle de l'école demain")
        @NotBlank @Size(max = 4000) String content,

        @Schema(description = "Niveau de gravité", example = "URGENT", allowableValues = {"INFO", "IMPORTANT", "URGENT"})
        @NotBlank String level,

        @Schema(description = "Genre d'audience", example = "ORGANISATION", allowableValues = {"ORGANISATION", "GROUP"})
        @NotBlank String audienceKind,

        @Schema(description = "Identifiant du groupe ciblé (requis si audienceKind = GROUP)")
        String groupId
) {}
