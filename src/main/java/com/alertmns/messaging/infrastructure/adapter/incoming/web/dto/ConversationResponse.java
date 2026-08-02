package com.alertmns.messaging.infrastructure.adapter.incoming.web.dto;

import com.alertmns.messaging.domain.model.ConversationKind;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Conversation de l'utilisateur courant")
public record ConversationResponse(
        @Schema(description = "Identifiant de la conversation") UUID conversationId,
        @Schema(description = "Genre de la conversation (DIRECT ou GROUP)") ConversationKind kind,
        @Schema(
                description = "Titre à afficher : nom du groupe, ou nom de l'interlocuteur pour un message "
                        + "direct. Résolu pour le lecteur, donc différent d'un participant à l'autre.",
                example = "Sofia Nkolo"
        ) String title,
        @Schema(description = "Identifiant du groupe (renseigné pour un groupe, null pour un DM)") UUID groupId,
        @Schema(description = "Membre en face, pour un message direct ; null pour un groupe")
        UUID counterpartMemberId,
        @Schema(description = "Dernier message échangé, ou null si la conversation est vide")
        LastMessageResponse lastMessage,
        @Schema(
                description = "Dernière activité : horodatage du dernier message, à défaut la création. "
                        + "C'est la clé de tri de la liste."
        ) Instant lastActivityAt,
        @Schema(description = "Date de création") Instant createdAt
) {

    @Schema(description = "Aperçu du dernier message d'une conversation")
    public record LastMessageResponse(
            @Schema(description = "Identifiant du message") UUID messageId,
            @Schema(description = "Identifiant du membre auteur") UUID authorId,
            @Schema(description = "Nom d'affichage de l'auteur") String authorName,
            @Schema(description = "Contenu textuel") String content,
            @Schema(description = "Horodatage d'envoi") Instant sentAt
    ) {}
}
