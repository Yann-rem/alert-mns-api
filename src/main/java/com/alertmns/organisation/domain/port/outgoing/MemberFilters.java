package com.alertmns.organisation.domain.port.outgoing;

import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;

import java.util.Collection;
import java.util.UUID;

/**
 * Critères de filtrage d'une liste de membres. Un champ {@code null} signifie « pas de filtre ».
 *
 * @param status      statut du membre, ou {@code null}
 * @param role        rôle du membre, ou {@code null}
 * @param userIds     restreint aux utilisateurs listés — résultat d'une recherche textuelle
 *                    résolue en amont via {@link UserDirectoryPort#searchIds(String)} ;
 *                    {@code null} si aucune recherche. Une collection <b>vide</b> signifie au
 *                    contraire « aucun résultat », et doit donc produire une liste vide.
 * @param groupId     restreint aux membres appartenant à ce groupe, ou {@code null}. Le filtre
 *                    traverse l'agrégat {@code GroupMembership}, qui matérialise l'appartenance :
 *                    l'adhésion n'est pas portée par le membre lui-même.
 */
public record MemberFilters(
        MemberStatus status,
        MemberRole role,
        Collection<UUID> userIds,
        UUID groupId
) {

    /** Aucun filtre : toute l'organisation. */
    public static MemberFilters none() {
        return new MemberFilters(null, null, null, null);
    }

    /** Les membres d'un groupe donné, sans autre restriction. */
    public static MemberFilters inGroup(UUID groupId) {
        return new MemberFilters(null, null, null, groupId);
    }
}
