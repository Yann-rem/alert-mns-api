package com.alertmns.shared;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant d'une organisation.
 */
public record OrganisationId(UUID value) {

    public OrganisationId {
        Objects.requireNonNull(value, "organisationId must not be null");
    }

    /**
     * Génère un nouvel identifiant aléatoire.
     *
     * @return un nouvel identifiant
     */
    public static OrganisationId generate() {
        return new OrganisationId(UUID.randomUUID());
    }

    /**
     * Reconstruit un identifiant depuis un UUID existant.
     *
     * @param value l'UUID source
     * @return l'identifiant reconstitué
     */
    public static OrganisationId from(UUID value) {
        return new OrganisationId(value);
    }

    /**
     * Reconstruit un identifiant depuis sa représentation textuelle.
     *
     * @param value la chaîne UUID
     * @return l'identifiant reconstitué
     * @throws IllegalArgumentException si le format UUID est invalide
     */
    public static OrganisationId from(String value) {
        return new OrganisationId(UUID.fromString(value));
    }
}
