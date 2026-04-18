package com.alertmns.organisation.domain.exception;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.MemberId;

/**
 * Exception de domaine représentant l'absence d'une adhésion recherchée pour un couple
 * (groupe, membre).
 */
public final class GroupMembershipNotFoundException extends RuntimeException {
    public GroupMembershipNotFoundException(GroupId groupId, MemberId memberId) {
        super("Membership not found in group " + groupId.value() + ": " + memberId.value());
    }
}
