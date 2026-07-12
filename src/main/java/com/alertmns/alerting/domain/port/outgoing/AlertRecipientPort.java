package com.alertmns.alerting.domain.port.outgoing;

import com.alertmns.shared.OrganisationId;

import java.util.List;
import java.util.UUID;

/**
 * Port sortant d'anti-corruption résolvant les destinataires d'une alerte auprès du BC Organisation.
 *
 * <p>Renvoie des {@code userId} (clé stable, opaque) plutôt que des membres : c'est cette clé qui identifie une
 * connexion WebSocket. Doublon d'ACL assumé par BC (ISP), à l'image de {@code GroupMembershipPort}.</p>
 */
public interface AlertRecipientPort {

    /**
     * @param organisationId l'organisation ciblée
     * @return les {@code userId} de tous les membres de l'organisation
     */
    List<UUID> organisationRecipients(OrganisationId organisationId);

    /**
     * @param groupId le groupe ciblé
     * @return les {@code userId} de tous les membres du groupe
     */
    List<UUID> groupRecipients(UUID groupId);
}
