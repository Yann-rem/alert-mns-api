package com.alertmns.messaging.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Réponse de création d'une conversation directe")
public record CreateDirectConversationResponse(
        @Schema(description = "Identifiant de la conversation") UUID conversationId
) {}
