package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.port.incoming.command.ListGroupsQuery;

import java.util.List;

/**
 * Port entrant : liste paginée des groupes d'une organisation (ADR-0014).
 *
 * <p>Alimente l'administration des groupes ainsi que le sélecteur d'audience de la diffusion
 * d'alerte — d'où une lecture ouverte aux rôles autorisés à diffuser, et pas aux seuls ADMIN.</p>
 *
 * <p>Implémenté par {@code com.alertmns.organisation.application.ListGroupsService}.</p>
 */
public interface ListGroupsUseCase {

    /** Page de résultats, accompagnée du total correspondant au filtre. */
    record GroupsPage(List<Group> groups, long total) {}

    GroupsPage list(ListGroupsQuery query);
}
