package com.alertmns.organisation.infrastructure.adapter.incoming.web;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.port.incoming.CreateGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.ListGroupsUseCase;
import com.alertmns.organisation.domain.port.incoming.ListGroupsUseCase.GroupsPage;
import com.alertmns.organisation.domain.port.incoming.RenameGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.ListGroupsQuery;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateGroupRequest;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateGroupResponse;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.GroupsPageResponse;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.RenameGroupRequest;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper.GroupWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Adapter web — administration des groupes. Réservé au rôle {@code ADMIN} : l'autorisation par rôle (RBAC) est un
 * concern d'authentification/livraison, placé sur l'adapter entrant pour garder l'application et le domaine
 * agnostiques de Spring Security. Le rôle est dérivé du {@code Member.role} du BC Organisation.
 */
@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Groups", description = "Gestion des groupes")
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final RenameGroupUseCase renameGroupUseCase;
    private final ListGroupsUseCase listGroupsUseCase;

    @Operation(
            summary = "Lister les groupes",
            description = """
                    Liste paginée et filtrable des groupes de l'organisation. Ouverte aux MANAGER en \
                    plus des ADMIN : le sélecteur d'audience de la diffusion d'alerte s'en sert, et \
                    la diffusion est justement permise aux deux rôles.""",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page de groupes"),
                    @ApiResponse(responseCode = "403", description = "Réservé aux rôles ADMIN et MANAGER")
            }
    )
    @GetMapping("/{orgId}/groups")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public GroupsPageResponse list(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId,
            @Parameter(description = "Recherche sur le nom du groupe")
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        GroupsPage result = listGroupsUseCase.list(new ListGroupsQuery(orgId.toString(), q, page, size));
        return GroupWebMapper.toGroupsPageResponse(result, page, size);
    }

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