package com.alertmns.messaging.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Message d'une conversation")
public record MessageResponse(
        @Schema(description = "Identifiant du message") UUID messageId,
        @Schema(description = "Identifiant du membre auteur") UUID authorId,
        @Schema(
                description = "Nom d'affichage de l'auteur, résolu à la lecture. Vaut "
                        + "« Utilisateur supprimé » si le membre a été anonymisé ou n'existe plus.",
                example = "Sofia Nkolo"
        ) String authorName,
        @Schema(description = "Contenu textuel") String content,
        @Schema(description = "Identifiant du message auquel celui-ci répond, ou null pour un message racine")
        UUID replyToMessageId,
        @Schema(description = "Horodatage d'envoi") Instant sentAt
) {}
