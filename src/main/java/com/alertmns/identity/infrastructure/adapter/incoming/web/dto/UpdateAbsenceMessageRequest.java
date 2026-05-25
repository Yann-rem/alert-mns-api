package com.alertmns.identity.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de mise à jour du message d'absence d'un utilisateur")
public record UpdateAbsenceMessageRequest(
        @Schema(description = "Contenu du message d'absence", example = "Je suis absent jusqu'au 15 avril.")
        @NotBlank @Size(max = 500) String content,

        @Schema(description = "Statut d'activation du message", example = "true")
        boolean active
) {}
