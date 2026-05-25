package com.alertmns.identity.domain.port.outgoing;

import com.alertmns.shared.UserId;

import java.util.List;

/**
 * Port sortant fournissant les autorités de sécurité d'un utilisateur.
 */
public interface UserAuthoritiesProvider {

    /**
     * Retourne les autorités de l'utilisateur, dérivées de son éventuel {@code Member}.
     *
     * @param userId identifiant de l'utilisateur
     * @return liste d'autorités au format Spring Security, ou liste vide si aucune membership
     */
    List<String> findAuthorities(UserId userId);
}
