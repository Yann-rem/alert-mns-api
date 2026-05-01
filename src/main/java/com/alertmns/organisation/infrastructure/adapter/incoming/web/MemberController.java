package com.alertmns.organisation.infrastructure.adapter.incoming.web;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.incoming.ActivateMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.InviteMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.ReactivateMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.SuspendMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.ActivateMemberCommand;
import com.alertmns.organisation.domain.port.incoming.command.ReactivateMemberCommand;
import com.alertmns.organisation.domain.port.incoming.command.SuspendMemberCommand;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.InviteMemberRequest;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.InviteMemberResponse;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper.MemberWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Gestion des membres d'une organisation")
public class MemberController {

    private final InviteMemberUseCase inviteMemberUseCase;
    private final ActivateMemberUseCase activateMemberUseCase;
    private final SuspendMemberUseCase suspendMemberUseCase;
    private final ReactivateMemberUseCase reactivateMemberUseCase;

    @Operation(
            summary = "Inviter un membre",
            description = "Crée un nouveau membre dans l'organisation avec le statut PENDING.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Membre invité avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "404", description = "Organisation introuvable"),
                    @ApiResponse(responseCode = "409", description = "Membre déjà présent dans l'organisation")
            }
    )
    @PostMapping("/{orgId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteMemberResponse invite(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId,
            @Valid @RequestBody InviteMemberRequest request
    ) {
        MemberId id = inviteMemberUseCase.invite(MemberWebMapper.toInviteMemberCommand(orgId, request));
        return new InviteMemberResponse(id.value());
    }

    @Operation(
            summary = "Activer un membre",
            description = "Passe le statut d'un membre de PENDING à ACTIVE.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Membre activé avec succès"),
                    @ApiResponse(responseCode = "403", description = "Membre rattaché à une autre organisation"),
                    @ApiResponse(responseCode = "404", description = "Membre introuvable"),
                    @ApiResponse(responseCode = "409", description = "Statut incompatible")
            }
    )
    @PostMapping("/{orgId}/members/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId,
            @Parameter(description = "Identifiant du membre") @PathVariable UUID id
    ) {
        activateMemberUseCase.activate(new ActivateMemberCommand(orgId.toString(), id.toString()));
    }

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
}
