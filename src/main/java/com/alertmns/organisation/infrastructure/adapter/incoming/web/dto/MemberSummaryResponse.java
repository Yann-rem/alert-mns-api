package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Membre d'une organisation, enrichi de l'identité de l'utilisateur")
public record MemberSummaryResponse(
        @Schema(description = "Identifiant du membre") UUID memberId,
        @Schema(description = "Identifiant de l'utilisateur") UUID userId,
        @Schema(description = "Prénom, ou null si l'utilisateur est introuvable") String firstName,
        @Schema(description = "Nom, ou null si l'utilisateur est introuvable") String lastName,
        @Schema(description = "Adresse e-mail, ou null si l'utilisateur est introuvable") String email,
        @Schema(description = "Rôle dans l'organisation", example = "ADMIN") String role,
        @Schema(description = "Statut du membre", example = "ACTIVE") String memberStatus,
        @Schema(
                description = "Statut du compte utilisateur, orthogonal au statut de membre (ADR-0019)",
                example = "ACTIVE"
        ) String accountStatus,
        @Schema(description = "Vrai si le compte a été anonymisé (RGPD)") boolean anonymized,
        @Schema(description = "Date d'entrée dans l'organisation") Instant joinedAt
) {}
