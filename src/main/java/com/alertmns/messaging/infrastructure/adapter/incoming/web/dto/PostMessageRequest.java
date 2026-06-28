package com.alertmns.messaging.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Schema(description = "Requête d'envoi d'un message")
public record PostMessageRequest(
        @Schema(description = "Contenu textuel du message")
        @NotBlank String content,

        @Schema(description = "Identifiant du message auquel répondre (optionnel)")
        UUID replyToMessageId
) {}
