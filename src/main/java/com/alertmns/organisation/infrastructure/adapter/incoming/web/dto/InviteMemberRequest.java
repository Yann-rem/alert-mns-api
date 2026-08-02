package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Invitation d'une personne à rejoindre l'organisation")
public record InviteMemberRequest(
        @Schema(description = "Adresse e-mail de l'invité", example = "prenom.nom@mns.fr")
        @NotBlank @Email @Size(max = 254) String email,

        @Schema(description = "Prénom", example = "Sofia")
        @NotBlank @Size(max = 100) String firstName,

        @Schema(description = "Nom", example = "Nkolo")
        @NotBlank @Size(max = 100) String lastName,

        @Schema(
                description = "Rôle attribué dans l'organisation",
                example = "MEMBER",
                allowableValues = {"ADMIN", "MANAGER", "MEMBER"}
        )
        @NotBlank
        @Pattern(regexp = "ADMIN|MANAGER|MEMBER", message = "role must be one of ADMIN, MANAGER, MEMBER")
        String role
) {}
