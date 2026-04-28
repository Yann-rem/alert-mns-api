package com.alertmns.iam.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Requête d'inscription d'un nouvel utilisateur")
public record RegisterUserRequest(
        @Schema(description = "Adresse email", example = "john.doe@example.com")
        @NotBlank @Size(max = 254) String email,

        @Schema(description = "Mot de passe en clair", example = "P@ssw0rd!")
        @NotBlank @Size(min = 8, max = 72) String rawPassword,

        @Schema(description = "Prénom", example = "John")
        @NotBlank @Size(max = 100) String firstName,

        @Schema(description = "Nom", example = "Doe")
        @NotBlank @Size(max = 100) String lastName,

        @Schema(description = "Identifiant de l'organisation de rattachement", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull UUID organisationId
) {}
