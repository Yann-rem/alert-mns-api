package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de la création d'un groupe")
public record CreateGroupRequest(
        @Schema(description = "Nom du groupe", example = "Concepteur Développeur d'Applications")
        @NotBlank @Size(max = 150) String name
) {}
