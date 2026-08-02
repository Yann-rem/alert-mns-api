package com.alertmns.organisation.infrastructure.adapter.incoming.web;

import com.alertmns.organisation.domain.port.incoming.CreateOrganisationUseCase;
import com.alertmns.organisation.domain.port.incoming.GetOrganisationUseCase;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateOrganisationRequest;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateOrganisationResponse;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.OrganisationResponse;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper.OrganisationWebMapper;
import com.alertmns.shared.OrganisationId;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Adapter web — création d'organisation. Réservé au rôle {@code ADMIN} (RBAC sur l'adapter entrant ; application et
 * domaine agnostiques de Spring Security). En mono-tenant, l'organisation est normalement provisionnée par le
 * bootstrap (hors HTTP) ; restreindre l'endpoint à {@code ADMIN} est le défaut sûr.
 */
@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Organisations", description = "Gestion des organisations")
public class OrganisationController {

    private final CreateOrganisationUseCase createOrganisationUseCase;
    private final GetOrganisationUseCase getOrganisationUseCase;

    @Operation(
            summary = "Consulter une organisation",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Détail de l'organisation"),
                    @ApiResponse(responseCode = "403", description = "Réservé au rôle ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Organisation introuvable")
            }
    )
    @GetMapping("/{orgId}")
    public OrganisationResponse get(
            @Parameter(description = "Identifiant de l'organisation") @PathVariable UUID orgId
    ) {
        return OrganisationWebMapper.toOrganisationResponse(
                getOrganisationUseCase.get(orgId.toString()));
    }

    @Operation(
            summary = "Créer une nouvelle organisation",
            description = "Crée une organisation avec le nom fourni.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Organisation créée avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "409", description = "Nom d'organisation déjà utilisé")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrganisationResponse create(@Valid @RequestBody CreateOrganisationRequest request) {
        OrganisationId id = createOrganisationUseCase.create(
                OrganisationWebMapper.toCreateOrganisationCommand(request)
        );
        return new CreateOrganisationResponse(id.value());
    }
}
