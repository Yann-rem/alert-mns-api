package com.alertmns.organisation.domain.model;

/**
 * Rôle d'un membre au sein d'une organisation.
 *
 * <p>Distinct de {@link com.alertmns.iam.domain.model.UserRole} qui représente les droits
 * sur la plateforme. Un USER côté IAM peut être ADMIN côté Organisation.</p>
 */
public enum MemberRole {
    ADMIN,
    MEMBER
}
