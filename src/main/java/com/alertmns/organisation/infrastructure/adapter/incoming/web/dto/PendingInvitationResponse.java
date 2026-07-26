package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Invitation en attente d'acceptation")
public record PendingInvitationResponse(
        @Schema(description = "Identifiant de l'invitation") UUID invitationId,
        @Schema(description = "Adresse e-mail de l'invité") String email,
        @Schema(description = "Prénom saisi à l'invitation, ou null") String firstName,
        @Schema(description = "Nom saisi à l'invitation, ou null") String lastName,
        @Schema(description = "Rôle qui sera attribué", example = "MEMBER") String role,
        @Schema(description = "Date d'émission") Instant createdAt,
        @Schema(description = "Date d'expiration") Instant expiresAt,
        @Schema(description = "Vrai si l'invitation a dépassé sa date d'expiration") boolean expired
) {}
