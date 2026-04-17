package com.alertmns.organisation.domain.exception;

import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.shared.OrganisationId;

/**
 * Exception de domaine représentant un conflit d'unicité : un groupe portant ce nom
 * existe déjà au sein de l'organisation ciblée.
 */
public final class GroupNameAlreadyExistsException extends RuntimeException {
    public GroupNameAlreadyExistsException(OrganisationId organisationId, GroupName name) {
        super("Group already exists in organisation " + organisationId.value()
                + ": " + name.value());
    }
}
