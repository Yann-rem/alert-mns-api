package com.alertmns.organisation.domain.exception;

import com.alertmns.shared.OrganisationId;

/**
 * Exception de domaine représentant une tentative d'opération entre agrégats
 * appartenant à des organisations différentes.
 */
public final class OrganisationMismatchException extends RuntimeException {
    public OrganisationMismatchException(OrganisationId expected, OrganisationId actual) {
        super("Organisation mismatch: expected " + expected.value() + ", got " + actual.value());
    }
}
