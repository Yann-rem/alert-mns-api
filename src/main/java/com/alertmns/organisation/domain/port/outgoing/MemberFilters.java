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
 */
public record MemberFilters(MemberStatus status, MemberRole role, Collection<UUID> userIds) {

    /** Aucun filtre : toute l'organisation. */
    public static MemberFilters none() {
        return new MemberFilters(null, null, null);
    }
}
