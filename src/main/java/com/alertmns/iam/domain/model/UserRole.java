package com.alertmns.iam.domain.model;

/**
 * Rôle d'un utilisateur dans le système.
 *
 * <p>Les rôles sont hiérarchiques : USER → MANAGER → ADMIN.</p>
 */
public enum UserRole {
    USER,
    MANAGER,
    ADMIN
}
