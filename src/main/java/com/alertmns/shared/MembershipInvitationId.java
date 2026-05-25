package com.alertmns.shared;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant unique d'une invitation à rejoindre une organisation.
 */
public record MembershipInvitationId(UUID value) {

    public MembershipInvitationId {
        Objects.requireNonNull(value, "membershipInvitationId must not be null");
    }

    /**
     * Génère un nouvel identifiant aléatoire.
     *
     * @return un nouvel identifiant
     */
    public static MembershipInvitationId generate() {
        return new MembershipInvitationId(UUID.randomUUID());
    }

    /**
     * Reconstruit un identifiant depuis un UUID existant.
     *
     * @param value l'UUID source
     * @return l'identifiant reconstitué
     */
    public static MembershipInvitationId from(UUID value) {
        return new MembershipInvitationId(value);
    }

    /**
     * Reconstruit un identifiant depuis sa représentation textuelle.
     *
     * @param value la chaîne UUID
     * @return l'identifiant reconstitué
     * @throws IllegalArgumentException si le format UUID est invalide
     */
    public static MembershipInvitationId from(String value) {
        return new MembershipInvitationId(UUID.fromString(value));
    }
}
