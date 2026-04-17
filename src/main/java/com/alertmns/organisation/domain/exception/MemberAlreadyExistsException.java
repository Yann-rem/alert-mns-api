package com.alertmns.organisation.domain.exception;

import com.alertmns.shared.OrganisationId;

import java.util.UUID;

/**
 * Exception de domaine représentant un conflit d'unicité : un utilisateur est déjà
 * membre de l'organisation ciblée.
 */
public class MemberAlreadyExistsException extends RuntimeException {
    public MemberAlreadyExistsException(OrganisationId organisationId, UUID userId) {
        super("Member already exists in organisation " + organisationId.value()
                + " for user " + userId);
    }
}
