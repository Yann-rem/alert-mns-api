package com.alertmns.identity.infrastructure.adapter.incoming.web;

import com.alertmns.identity.domain.port.incoming.AnonymizeUserUseCase;
import com.alertmns.identity.domain.port.incoming.ReactivateUserUseCase;
import com.alertmns.identity.domain.port.incoming.SuspendUserUseCase;
import com.alertmns.identity.domain.port.incoming.UpdateAbsenceMessageUseCase;
import com.alertmns.identity.domain.port.incoming.UpdateProfileUseCase;
import com.alertmns.identity.domain.port.incoming.command.AnonymizeUserCommand;
import com.alertmns.identity.domain.port.incoming.command.ReactivateUserCommand;
import com.alertmns.identity.domain.port.incoming.command.SuspendUserCommand;
import com.alertmns.identity.infrastructure.adapter.incoming.web.dto.UpdateAbsenceMessageRequest;
import com.alertmns.identity.infrastructure.adapter.incoming.web.dto.UpdateProfileRequest;
import com.alertmns.identity.infrastructure.adapter.incoming.web.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestion des utilisateurs")
public class UserController {

    private final SuspendUserUseCase suspendUserUseCase;
    private final ReactivateUserUseCase reactivateUserUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final UpdateAbsenceMessageUseCase updateAbsenceMessageUseCase;
    private final AnonymizeUserUseCase anonymizeUserUseCase;

    @Operation(
            summary = "Suspendre un utilisateur",
            description = "Passe le statut d'un utilisateur de ACTIVE à SUSPENDED.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Utilisateur suspendu avec succès"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspend(@Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id) {
        suspendUserUseCase.suspend(new SuspendUserCommand(id.toString()));
    }

    @Operation(
            summary = "Réactiver un utilisateur",
            description = "Passe le statut d'un utilisateur de SUSPENDED à ACTIVE.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Utilisateur réactivé avec succès"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable"),
                    @ApiResponse(responseCode = "409", description = "Statut incompatible")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivate(@Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id) {
        reactivateUserUseCase.reactivate(new ReactivateUserCommand(id.toString()));
    }

    @Operation(
            summary = "Anonymiser un utilisateur (RGPD)",
            description = "Efface les données personnelles (email, profil, mot de passe) en les remplaçant par des "
                    + "placeholders non-identifiants. Le userId est conservé. Opération idempotente, déclenchée par "
                    + "l'ADMIN sur instruction du DPO (droit à l'effacement, article 17 RGPD).",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Utilisateur anonymisé avec succès"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/anonymize")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void anonymize(@Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id) {
        anonymizeUserUseCase.anonymize(new AnonymizeUserCommand(id.toString()));
    }

    @Operation(
            summary = "Mettre à jour le profil",
            description = "Met à jour le prénom, le nom et l'avatar d'un utilisateur.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Profil mis à jour avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
            }
    )
    @PreAuthorize("#id.toString() == authentication.principal.userId.value().toString() or hasRole('ADMIN')")
    @PutMapping("/{id}/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(
            @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        updateProfileUseCase.update(UserWebMapper.toUpdateProfileCommand(id, request));
    }

    @Operation(
            summary = "Mettre à jour le message d'absence",
            description = "Met à jour le contenu et le statut d'activation du message d'absence d'un utilisateur.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Message d'absence mis à jour avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
            }
    )
    @PreAuthorize("#id.toString() == authentication.principal.userId.value().toString() or hasRole('ADMIN')")
    @PutMapping("/{id}/absence-message")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAbsenceMessage(
            @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id,
            @Valid @RequestBody UpdateAbsenceMessageRequest request
    ) {
        updateAbsenceMessageUseCase.update(UserWebMapper.toUpdateAbsenceMessageCommand(id, request));
    }
}
