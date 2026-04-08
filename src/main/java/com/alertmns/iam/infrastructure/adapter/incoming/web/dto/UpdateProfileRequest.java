package com.alertmns.iam.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de mise à jour du profil d'un utilisateur")
public record UpdateProfileRequest(
        @Schema(description = "Prénom", example = "John")
        @NotBlank @Size(max = 100) String firstName,

        @Schema(description = "Nom", example = "Doe")
        @NotBlank @Size(max = 100) String lastName,

        @Schema(description = "URL de l'avatar", example = "https://example.com/avatar.png", nullable = true)
        String avatar
) {}
