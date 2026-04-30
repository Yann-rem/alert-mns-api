package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Réponse après invitation d'un utilisateur en tant que membre")
public record InviteMemberResponse(
        @Schema(description = "Identifiant du membre créé", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id
) {}
