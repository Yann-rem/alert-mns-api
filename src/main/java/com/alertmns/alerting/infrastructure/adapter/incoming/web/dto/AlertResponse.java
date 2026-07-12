package com.alertmns.alerting.infrastructure.adapter.incoming.web.dto;

import com.alertmns.alerting.domain.model.AlertAudienceKind;
import com.alertmns.alerting.domain.model.AlertLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Alerte destinée à l'utilisateur courant")
public record AlertResponse(
        @Schema(description = "Identifiant de l'alerte") UUID alertId,
        @Schema(description = "Membre émetteur") UUID issuerId,
        @Schema(description = "Genre d'audience (ORGANISATION ou GROUP)") AlertAudienceKind audienceKind,
        @Schema(description = "Groupe ciblé (renseigné si audienceKind = GROUP, sinon null)") UUID groupId,
        @Schema(description = "Niveau de gravité") AlertLevel level,
        @Schema(description = "Contenu de l'alerte") String content,
        @Schema(description = "Date de diffusion") Instant issuedAt
) {}
