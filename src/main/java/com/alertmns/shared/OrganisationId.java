package com.alertmns.shared;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant d'une organisation.
 */
public final class OrganisationId {

    private final UUID value;

    private OrganisationId(UUID value) {
        Objects.requireNonNull(value, "organisationId must not be null");
        this.value = value;
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

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrganisationId that = (OrganisationId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "OrganisationId{" +
                "value=" + value +
                '}';
    }
}
