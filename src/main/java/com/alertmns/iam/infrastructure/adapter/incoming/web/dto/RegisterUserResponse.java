package com.alertmns.iam.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse après inscription d'un utilisateur")
public record RegisterUserResponse(
        @Schema(description = "Identifiant de l'utilisateur créé", example = "550e8400-e29b-41d4-a716-446655440000")
        String id
) {}
