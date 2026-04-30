package com.alertmns.organisation.domain.port.outgoing;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.MemberId;

import java.util.Optional;

/**
 * Port sortant pour la persistance des adhésions à un groupe.
 */
public interface GroupMembershipRepository {

    /**
     * Sauvegarde une adhésion (création ou mise à jour).
     *
     * @param groupMembership l'adhésion à sauvegarder
     */
    void save(GroupMembership groupMembership);

    /**
     * Supprime une adhésion existante.
     *
     * @param groupMembership l'adhésion à supprimer
     */
    void delete(GroupMembership groupMembership);

    /**
     * Recherche une adhésion par le couple (groupe, membre).
     *
     * @param groupId  l'identifiant du groupe
     * @param memberId l'identifiant du membre
     * @return l'adhésion trouvée, ou vide
     */
    Optional<GroupMembership> findByGroupIdAndMemberId(GroupId groupId, MemberId memberId);
}
