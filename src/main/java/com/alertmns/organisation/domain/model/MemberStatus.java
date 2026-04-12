package com.alertmns.organisation.domain.model;

/**
 * Statut du cycle de vie d'un membre au sein d'une organisation.
 *
 * <p>Transitions autorisées : PENDING → ACTIVE → SUSPENDED → ACTIVE (réactivation).</p>
 */
public enum MemberStatus {
    PENDING,
    ACTIVE,
    SUSPENDED
}
