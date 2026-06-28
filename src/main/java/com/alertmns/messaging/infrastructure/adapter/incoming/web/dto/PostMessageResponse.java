package com.alertmns.messaging.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Réponse d'envoi d'un message")
public record PostMessageResponse(
        @Schema(description = "Identifiant du message créé") UUID messageId
) {}
