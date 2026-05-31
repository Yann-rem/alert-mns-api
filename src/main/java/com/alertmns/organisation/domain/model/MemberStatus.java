package com.alertmns.organisation.domain.model;

/**
 * Statut du cycle de vie d'un membre.
 */
public enum MemberStatus {
    /**
     * @deprecated Vestige du cycle ({@code invite() → activate()}). Le « statut intermédiaire invité mais pas encore
     * membre » est désormais porté par {@link MembershipInvitation}. À retirer au prochain commit.
     */
    @Deprecated
    PENDING,
    ACTIVE,
    SUSPENDED,
    BANNED
}
