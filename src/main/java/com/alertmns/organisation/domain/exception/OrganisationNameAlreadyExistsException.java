package com.alertmns.organisation.domain.exception;

import com.alertmns.organisation.domain.model.OrganisationName;

/**
 * Exception de domaine représentant un conflit de nom déjà associé à une organisation existante.
 */
public final class OrganisationNameAlreadyExistsException extends RuntimeException {
    public OrganisationNameAlreadyExistsException(OrganisationName name) {
        super("Organisation already exists: " + name.value());
    }
}
