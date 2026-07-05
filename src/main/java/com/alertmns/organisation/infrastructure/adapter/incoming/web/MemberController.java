package com.alertmns.organisation.infrastructure.adapter.incoming.web;

import com.alertmns.organisation.domain.port.incoming.ChangeMemberRoleUseCase;
import com.alertmns.organisation.domain.port.incoming.ReactivateMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.SuspendMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.ChangeMemberRoleCommand;
import com.alertmns.organisation.domain.port.incoming.command.ReactivateMemberCommand;
import com.alertmns.organisation.domain.port.incoming.command.SuspendMemberCommand;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.ChangeMemberRoleRequest;
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

/**
 * Adapter web — administration des membres (suspension / réactivation / changement de rôle). Réservé au rôle
 * {@code ADMIN} : le RBAC est
 * placé sur l'adapter entrant pour garder l'application et le domaine agnostiques de Spring Security. Le rôle est
 * dérivé du {@code Member.role} du BC Organisation.
 */
@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Members", description = "Gestion des membres d'une organisation")
public class MemberController {

    private final SuspendMemberUseCase suspendMemberUseCase;
    private final ReactivateMemberUseCase reactivateMemberUseCase;
    private final ChangeMemberRoleUseCase changeMemberRoleUseCase;

    @Operation(
            summary = "Suspendre un membre",
            description = "Passe le statut d'un membre de ACTIVE à SUSPENDED.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Membre suspendu avec succès"),
                    @ApiResponse(responseCode = "403", description = "Membre rattaché à une autre organisation"),
                    @ApiResponse(responseCode = "404", description = "Membre introuvable"),
                    @ApiResponse(responseCode = "409", description = "Statut incompatible")
            }
    )
    @PostMapping("/{orgId}/members/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspend(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId,
            @Parameter(description = "Identifiant du membre") @PathVariable UUID id
    ) {
        suspendMemberUseCase.suspend(new SuspendMemberCommand(orgId.toString(), id.toString()));
    }

    @Operation(
            summary = "Réactiver un membre",
            description = "Passe le statut d'un membre de SUSPENDED à ACTIVE.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Membre réactivé avec succès"),
                    @ApiResponse(responseCode = "403", description = "Membre rattaché à une autre organisation"),
                    @ApiResponse(responseCode = "404", description = "Membre introuvable"),
                    @ApiResponse(responseCode = "409", description = "Statut incompatible")
            }
    )
    @PostMapping("/{orgId}/members/{id}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivate(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId,
            @Parameter(description = "Identifiant du membre") @PathVariable UUID id
    ) {
        reactivateMemberUseCase.reactivate(new ReactivateMemberCommand(orgId.toString(), id.toString()));
    }

    @Operation(
            summary = "Changer le rôle d'un membre",
            description = "Change le rôle d'un membre. Refuse de rétrograder le dernier administrateur actif.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Rôle changé avec succès"),
                    @ApiResponse(responseCode = "400", description = "Rôle invalide"),
                    @ApiResponse(responseCode = "403", description = "Membre rattaché à une autre organisation"),
                    @ApiResponse(responseCode = "404", description = "Membre introuvable"),
                    @ApiResponse(responseCode = "409", description = "Dernier administrateur actif : rétrogradation refusée")
            }
    )
    @PutMapping("/{orgId}/members/{id}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRole(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId,
            @Parameter(description = "Identifiant du membre") @PathVariable UUID id,
            @Valid @RequestBody ChangeMemberRoleRequest request
    ) {
        changeMemberRoleUseCase.changeRole(
                new ChangeMemberRoleCommand(orgId.toString(), id.toString(), request.role()));
    }
}
