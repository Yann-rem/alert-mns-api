package com.alertmns.organisation.infrastructure.adapter.incoming.web;

import com.alertmns.organisation.domain.port.incoming.CreateOrganisationUseCase;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateOrganisationRequest;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateOrganisationResponse;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper.OrganisationWebMapper;
import com.alertmns.shared.OrganisationId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@Tag(name = "Organisations", description = "Gestion des organisations")
public class OrganisationController {

    private final CreateOrganisationUseCase createOrganisationUseCase;

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
        OrganisationId id = createOrganisationUseCase.create(OrganisationWebMapper.toCommand(request));
        return new CreateOrganisationResponse(id.value());
    }
}
