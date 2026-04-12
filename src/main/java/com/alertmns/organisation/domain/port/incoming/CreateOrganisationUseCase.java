package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.port.incoming.command.CreateOrganisationCommand;
import com.alertmns.shared.OrganisationId;

/**
 * Port entrant représentant le cas d'utilisation de création d'une organisation.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.CreateOrganisationService}.</p>
 */
public interface CreateOrganisationUseCase {

    /**
     * Crée une nouvelle organisation.
     *
     * @param command la commande de création
     * @return l'identifiant de l'organisation créée
     */
    OrganisationId create(CreateOrganisationCommand command);
}
