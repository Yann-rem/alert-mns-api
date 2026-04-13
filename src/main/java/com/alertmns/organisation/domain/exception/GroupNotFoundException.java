package com.alertmns.organisation.domain.exception;

import com.alertmns.organisation.domain.model.GroupId;

/**
 * Exception de domaine représentant l'absence d'un groupe recherché par son identifiant.
 */
public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(GroupId id) {
        super("Group not found: " + id.value());
    }
}
