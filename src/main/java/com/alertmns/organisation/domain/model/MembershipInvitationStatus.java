package com.alertmns.organisation.domain.model;

/**
 * Statut du cycle de vie d'une {@link MembershipInvitation}.
 *
 * <p>Transitions autorisées :</p>
 * <ul>
 *     <li>{@code PENDING → ACCEPTED} : l'invité a accepté.</li>
 *     <li>{@code PENDING → EXPIRED} : le TTL est écoulé sans acceptation.</li>
 * </ul>
 *
 * <p>Aucune transition possible depuis {@code ACCEPTED}, {@code EXPIRED} ou {@code REVOKED} : ces statuts sont
 * terminaux.</p>
 */
public enum MembershipInvitationStatus {
    PENDING,
    ACCEPTED,
    EXPIRED,
    REVOKED
}
