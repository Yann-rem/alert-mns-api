package com.alertmns.organisation.infrastructure.adapter.incoming.web;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.port.incoming.CreateGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.RenameGroupUseCase;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateGroupRequest;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateGroupResponse;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.RenameGroupRequest;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper.GroupWebMapper;
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
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Gestion des groupes")
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final RenameGroupUseCase renameGroupUseCase;

    @Operation(
            summary = "Créer un nouveau groupe",
            description = "Crée un groupe au sein d'une organisation.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Groupe créé avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "404", description = "Organisation introuvable"),
                    @ApiResponse(responseCode = "409", description = "Nom de groupe déjà utilisé")
            }
    )
    @PostMapping("/{orgId}/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGroupResponse create(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        GroupId id = createGroupUseCase.create(GroupWebMapper.toCreateGroupCommand(orgId, request));
        return new CreateGroupResponse(id.value());
    }

    @Operation(
            summary = "Renommer un groupe",
            description = "Met à jour le nom d'un groupe au sein d'une organisation.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Groupe renommé avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "403", description = "Groupe rattaché à une autre organisation"),
                    @ApiResponse(responseCode = "404", description = "Groupe introuvable"),
                    @ApiResponse(responseCode = "409", description = "Nom de groupe déjà utilisé")
            }
    )
    @PutMapping("/{orgId}/groups/{id}/name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rename(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId,
            @Parameter(description = "Identifiant du groupe") @PathVariable UUID id,
            @Valid @RequestBody RenameGroupRequest request
    ) {
        renameGroupUseCase.rename(GroupWebMapper.toRenameGroupCommand(orgId, id, request));
    }
}