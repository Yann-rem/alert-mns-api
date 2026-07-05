package com.alertmns.messaging.infrastructure.adapter.incoming.web.dto;

import com.alertmns.messaging.domain.model.ConversationKind;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Conversation de l'utilisateur courant")
public record ConversationResponse(
        @Schema(description = "Identifiant de la conversation") UUID conversationId,
        @Schema(description = "Genre de la conversation (DIRECT ou GROUP)") ConversationKind kind,
        @Schema(description = "Nom de la conversation (renseigné pour un groupe, null pour un DM)") String name,
        @Schema(description = "Identifiant du groupe (renseigné pour un groupe, null pour un DM)") UUID groupId,
        @Schema(description = "Premier participant (renseigné pour un DM, null pour un groupe)") UUID participantOne,
        @Schema(description = "Second participant (renseigné pour un DM, null pour un groupe)") UUID participantTwo,
        @Schema(description = "Date de création") Instant createdAt
) {}
