package com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Utilisateur connecté et son adhésion à l'organisation courante")
public record MeResponse(
        @Schema(description = "Identifiant de l'utilisateur") String userId,
        @Schema(description = "Adresse e-mail") String email,
        @Schema(description = "Prénom") String firstName,
        @Schema(description = "Nom") String lastName,
        @Schema(
                description = "Organisation courante, ou null si l'utilisateur n'est membre d'aucune "
                        + "organisation. Le client s'en sert pour construire les URLs "
                        + "/api/organisations/{orgId}/… (ADR-0011)."
        ) String organisationId,
        @Schema(description = "Rôle métier dans l'organisation, ou null", example = "ADMIN") String role,
        @Schema(description = "Statut de l'adhésion, ou null", example = "ACTIVE") String memberStatus,
        @Schema(description = "Message d'absence, ou null s'il n'a jamais été défini")
        AbsenceMessageResponse absenceMessage
) {

    @Schema(description = "Message d'absence affiché aux personnes qui écrivent à l'utilisateur")
    public record AbsenceMessageResponse(
            @Schema(description = "Contenu du message") String content,
            @Schema(description = "Vrai si le message est actif") boolean active
    ) {}
}
