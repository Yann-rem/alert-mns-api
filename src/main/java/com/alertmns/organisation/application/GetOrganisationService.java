package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.OrganisationNotFoundException;
import com.alertmns.organisation.domain.model.Organisation;
import com.alertmns.organisation.domain.port.incoming.GetOrganisationUseCase;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.shared.OrganisationId;

import java.util.Objects;

/** Détail d'une organisation. */
public class GetOrganisationService implements GetOrganisationUseCase {

    private final OrganisationRepository organisationRepository;

    public GetOrganisationService(OrganisationRepository organisationRepository) {
        this.organisationRepository = Objects.requireNonNull(
                organisationRepository, "organisationRepository must not be null");
    }

    @Override
    public Organisation get(String organisationId) {
        Objects.requireNonNull(organisationId, "organisationId must not be null");

        OrganisationId id = OrganisationId.from(organisationId);
        return organisationRepository.findById(id)
                .orElseThrow(() -> new OrganisationNotFoundException(id));
    }
}
