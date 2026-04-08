package com.alertmns.iam.domain.model;

/**
 * Statut du cycle de vie d'un utilisateur.
 *
 * <p>Transitions autorisées : PENDING → ACTIVE → DISABLED → ACTIVE (réactivation).</p>
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    DISABLED
}
