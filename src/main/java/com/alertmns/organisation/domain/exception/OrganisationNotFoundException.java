package com.alertmns.organisation.domain.exception;

import com.alertmns.shared.OrganisationId;

/**
 * Exception de domaine représentant l'absence d'une organisation recherchée par son identifiant.
 */
public class OrganisationNotFoundException extends RuntimeException {
    public OrganisationNotFoundException(OrganisationId id) {
        super("Organisation not found: " + id.value());
    }
}
