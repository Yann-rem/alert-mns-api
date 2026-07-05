package com.alertmns.messaging.domain.port.outgoing;

import java.util.List;
import java.util.UUID;

/**
 * Port sortant (ACL) par lequel le BC Messaging interroge l'appartenance aux groupes — information détenue par le
 * BC Organisation.
 *
 * <p>Étend {@link GroupMembershipChecker} (« ce membre est-il dans CE groupe ? ») avec l'énumération des groupes
 * d'un membre. L'agrégat {@code Conversation} ne dépend que de {@link GroupMembershipChecker} (vérification
 * ponctuelle) ; seuls les services qui doivent lister les groupes d'un membre dépendent de ce port élargi
 * (ségrégation d'interface).</p>
 */
public interface GroupMembershipPort extends GroupMembershipChecker {

    /**
     * Liste les identifiants des groupes auxquels le membre appartient.
     *
     * @param memberId l'identifiant du membre
     * @return les identifiants de groupes (éventuellement vide)
     */
    List<UUID> groupIdsOf(UUID memberId);
}
