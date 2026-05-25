package com.alertmns.organisation.domain.model;

/**
 * Rôle d'un membre au sein d'une organisation.
 *
 * <p>Source de vérité unique pour les rôles métier de la plateforme. Le BC Identity en dérive les autorités Spring
 * Security via {@code UserAuthoritiesProvider}.</p>
 */
public enum MemberRole {
    ADMIN,
    MEMBER
}
