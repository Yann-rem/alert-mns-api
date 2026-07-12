package com.alertmns.alerting.domain.port.outgoing;

import java.util.List;
import java.util.UUID;

/**
 * Port sortant (ACL) donnant les groupes d'un membre, pour cibler « mes alertes ».
 *
 * <p>Propre au BC Alerting : chaque bounded context possède son propre port vers l'appartenance aux groupes du BC
 * Organisation.</p>
 */
public interface GroupMembershipPort {

    /**
     * Retourne les identifiants des groupes auxquels le membre appartient.
     *
     * @param memberId identifiant du membre
     * @return la liste des identifiants de groupe (éventuellement vide)
     */
    List<UUID> groupIdsOf(UUID memberId);
}
