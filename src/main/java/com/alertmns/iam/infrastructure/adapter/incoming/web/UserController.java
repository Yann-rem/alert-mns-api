package com.alertmns.iam.infrastructure.adapter.incoming.web;

import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.ActivateUserUseCase;
import com.alertmns.iam.domain.port.incoming.DisableUserUseCase;
import com.alertmns.iam.domain.port.incoming.ReactivateUserUseCase;
import com.alertmns.iam.domain.port.incoming.RegisterUserUseCase;
import com.alertmns.iam.domain.port.incoming.UpdateAbsenceMessageUseCase;
import com.alertmns.iam.domain.port.incoming.UpdateProfileUseCase;
import com.alertmns.iam.domain.port.incoming.command.ActivateUserCommand;
import com.alertmns.iam.domain.port.incoming.command.DisableUserCommand;
import com.alertmns.iam.domain.port.incoming.command.ReactivateUserCommand;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.RegisterUserRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.RegisterUserResponse;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.UpdateAbsenceMessageRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.UpdateProfileRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    private final RegisterUserUseCase registerUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final DisableUserUseCase disableUserUseCase;
    private final ReactivateUserUseCase reactivateUserUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final UpdateAbsenceMessageUseCase updateAbsenceMessageUseCase;

    @Operation(
            summary = "Inscrire un nouvel utilisateur",
            description = "Crée un compte utilisateur avec le statut PENDING, en attente d'activation par un administrateur.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Utilisateur inscrit avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
            }
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        UserId id = registerUserUseCase.register(UserWebMapper.toCommand(request));
        return new RegisterUserResponse(id.value());
    }

    @Operation(
            summary = "Activer un utilisateur",
            description = "Passe le statut d'un utilisateur de PENDING à ACTIVE.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Utilisateur activé avec succès"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
            }
    )
    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id) {
        activateUserUseCase.activate(new ActivateUserCommand(id.toString()));
    }

    @Operation(
            summary = "Désactiver un utilisateur",
            description = "Passe le statut d'un utilisateur de ACTIVE à DISABLED.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Utilisateur désactivé avec succès"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
            }
    )
    @PostMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id) {
        disableUserUseCase.disable(new DisableUserCommand(id.toString()));
    }

    @Operation(
            summary = "Réactiver un utilisateur",
            description = "Passe le statut d'un utilisateur de DISABLED à ACTIVE.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Utilisateur réactivé avec succès"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable"),
                    @ApiResponse(responseCode = "409", description = "Statut incompatible")
            }
    )
    @PostMapping("/{id}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivate(@Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id) {
        reactivateUserUseCase.reactivate(new ReactivateUserCommand(id.toString()));
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
    @PutMapping("/{id}/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(
            @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        updateProfileUseCase.update(UserWebMapper.toCommand(id, request));
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
    @PutMapping("/{id}/absence-message")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAbsenceMessage(
            @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id,
            @Valid @RequestBody UpdateAbsenceMessageRequest request
    ) {
        updateAbsenceMessageUseCase.update(UserWebMapper.toCommand(id, request));
    }
}
