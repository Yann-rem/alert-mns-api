package com.alertmns.iam.domain.model;

/**
 * Statut du cycle de vie d'un utilisateur.
 *
 * <p>Transitions autorisées : PENDING → ACTIVE → SUSPENDED → ACTIVE (réactivation). BANNED est terminal</p>
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    BANNED
}
