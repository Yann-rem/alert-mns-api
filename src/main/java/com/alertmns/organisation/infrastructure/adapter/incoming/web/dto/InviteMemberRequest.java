package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import com.alertmns.organisation.domain.model.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Requête d'invitation d'un utilisateur en tant que membre")
public record InviteMemberRequest(
        @Schema(description = "Identifiant de l'utilisateur invité", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull UUID userId,

        @Schema(description = "Rôle attribué au membre", example = "MEMBER")
        @NotNull MemberRole role
) {}
