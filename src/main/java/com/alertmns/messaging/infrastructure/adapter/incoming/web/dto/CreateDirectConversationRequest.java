package com.alertmns.messaging.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Requête de création d'une conversation directe")
public record CreateDirectConversationRequest(
        @Schema(description = "Identifiant du membre cible")
        @NotNull UUID targetMemberId
) {}
