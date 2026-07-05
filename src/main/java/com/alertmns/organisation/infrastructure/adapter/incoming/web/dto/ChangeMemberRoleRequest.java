package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête de changement de rôle d'un membre")
public record ChangeMemberRoleRequest(
        @Schema(description = "Nouveau rôle", example = "MANAGER", allowableValues = {"ADMIN", "MANAGER", "MEMBER"})
        @NotBlank String role
) {}
