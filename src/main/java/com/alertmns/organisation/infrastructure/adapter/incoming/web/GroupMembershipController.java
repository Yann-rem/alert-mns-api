package com.alertmns.organisation.infrastructure.adapter.incoming.web;

import com.alertmns.organisation.domain.port.incoming.AddMemberToGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.RemoveMemberFromGroupUseCase;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper.GroupMembershipWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Adapter web — administration des adhésions membre/groupe. Réservé au rôle {@code ADMIN} : le RBAC est placé sur
 * l'adapter entrant pour garder l'application et le domaine agnostiques de Spring Security. Le rôle est dérivé du
 * {@code Member.role} du BC Organisation.
 */
@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Memberships", description = "Gestion des adhésions des membres aux groupes")
public class GroupMembershipController {

    private final AddMemberToGroupUseCase addMemberToGroupUseCase;
    private final RemoveMemberFromGroupUseCase removeMemberFromGroupUseCase;

    @Operation(
            summary = "Ajouter un membre à un groupe",
            description = "Crée l'adhésion (groupe, membre) si elle n'existe pas. Opération idempotente : "
                    + "rejouer la même requête laisse l'état inchangé.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Adhésion en place"),
                    @ApiResponse(responseCode = "403", description = "Groupe ou membre rattaché à une autre organisation"),
                    @ApiResponse(responseCode = "404", description = "Groupe ou membre introuvable")
            }
    )
    @PutMapping("/{orgId}/groups/{groupId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void add(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId,
            @Parameter(description = "Identifiant du groupe") @PathVariable UUID groupId,
            @Parameter(description = "Identifiant du membre") @PathVariable UUID memberId
    ) {
        addMemberToGroupUseCase.add(GroupMembershipWebMapper.toAddMemberToGroupCommand(orgId, groupId, memberId));
    }

    @Operation(
            summary = "Retirer un membre d'un groupe",
            description = "Supprime l'adhésion (groupe, membre).",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Adhésion supprimée avec succès"),
                    @ApiResponse(responseCode = "403", description = "Adhésion rattachée à une autre organisation"),
                    @ApiResponse(responseCode = "404", description = "Adhésion introuvable")
            }
    )
    @DeleteMapping("/{orgId}/groups/{groupId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId,
            @Parameter(description = "Identifiant du groupe") @PathVariable UUID groupId,
            @Parameter(description = "Identifiant du membre") @PathVariable UUID memberId
    ) {
        removeMemberFromGroupUseCase.remove(
                GroupMembershipWebMapper.toRemoveMemberFromGroupCommand(orgId, groupId, memberId)
        );
    }
}
