package com.alertmns.organisation.domain.exception;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.MemberId;

/**
 * Exception de domaine représentant un conflit d'unicité : un membre fait déjà partie
 * du groupe ciblé.
 */
public final class GroupMembershipAlreadyExistsException extends RuntimeException {
    public GroupMembershipAlreadyExistsException(GroupId groupId, MemberId memberId) {
        super("Membership already exists in group " + groupId.value() + ": " + memberId.value());
    }
}
