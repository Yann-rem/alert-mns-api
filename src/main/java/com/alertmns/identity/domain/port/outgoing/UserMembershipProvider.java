package com.alertmns.identity.domain.port.outgoing;

import com.alertmns.shared.UserId;

import java.util.Optional;
import java.util.UUID;

/**
 * Port sortant exposant l'adhésion d'un utilisateur à son organisation.
 *
 * <p>Complète {@link UserAuthoritiesProvider}, qui ne renvoie que les autorités Spring Security :
 * ici on expose les informations dont le client a besoin pour se situer (organisation courante,
 * rôle métier, statut d'adhésion).</p>
 *
 * <p>Le BC Identity ne stocke pas l'organisation de l'utilisateur — contrairement à ce que prévoyait
 * ADR-0003, le champ n'a jamais été implémenté sur {@code User}. L'information appartient donc au
 * seul BC Organisation, d'où ce port.</p>
 */
public interface UserMembershipProvider {

    /**
     * @param userId identifiant de l'utilisateur
     * @return son adhésion, ou vide s'il n'est membre d'aucune organisation
     *         (compte invité mais pas encore activé, ou banni)
     */
    Optional<Membership> findByUserId(UserId userId);

    /**
     * @param organisationId organisation d'appartenance
     * @param role           rôle métier (ADMIN, MEMBER…)
     * @param status         statut de l'adhésion (ACTIVE, SUSPENDED…), orthogonal au statut du
     *                       compte utilisateur (ADR-0019)
     */
    record Membership(UUID organisationId, String role, String status) {}
}
