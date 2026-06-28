package com.alertmns.messaging.domain.port.outgoing;

import java.util.UUID;

/**
 * Port sortant (ACL) permettant au BC Messaging de vérifier l'appartenance d'un membre à un groupe, information
 * détenue par le BC Organisation.
 */
@FunctionalInterface
public interface GroupMembershipChecker {

    /**
     * Indique si le membre appartient au groupe.
     *
     * @param groupId  l'identifiant du groupe
     * @param memberId l'identifiant du membre
     * @return {@code true} si le membre est dans le groupe
     */
    boolean isMember(UUID groupId, UUID memberId);
}
