package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Groupe d'une organisation")
public record GroupSummaryResponse(
        @Schema(description = "Identifiant du groupe") UUID groupId,
        @Schema(description = "Nom du groupe", example = "Promo CDA 2026") String name,
        @Schema(
                description = "Nature du groupe. GENERAL désigne le canal par défaut de l'organisation, "
                        + "auquel tout nouveau membre est rattaché automatiquement.",
                example = "STANDARD"
        ) String kind,
        @Schema(description = "Date de création") Instant createdAt
) {}
