package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.model.Organisation;

/**
 * Port entrant : détail d'une organisation (ADR-0014).
 *
 * <p>Implémenté par {@code com.alertmns.organisation.application.GetOrganisationService}.</p>
 */
public interface GetOrganisationUseCase {

    /**
     * @param organisationId identifiant de l'organisation
     * @return l'organisation
     * @throws com.alertmns.organisation.domain.exception.OrganisationNotFoundException si elle n'existe pas
     */
    Organisation get(String organisationId);
}
