package com.alertmns.organisation.domain.model;

/**
 * Rôle d'un membre au sein d'une organisation.
 *
 * <p>Source de vérité unique pour les rôles métier de la plateforme. Le BC Identity en dérive les autorités Spring
 * Security via {@code UserAuthoritiesProvider}.</p>
 */
public enum MemberRole {

    /** Administrateur de l'organisation : toutes les opérations d'administration. */
    ADMIN,

    /** Gestionnaire de chat : peut diffuser des alertes et modérer (introduit avec le BC Alerting). */
    MANAGER,

    /** Membre standard. */
    MEMBER
}
