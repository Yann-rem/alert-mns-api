package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.GroupMembershipId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;

/**
 * Événement de domaine représentant le retrait d'un membre d'un groupe.
 */
public record MemberRemovedFromGroup(GroupMembershipId groupMembershipId, Instant occurredOn) implements DomainEvent {

    public MemberRemovedFromGroup(GroupMembershipId groupMembershipId) {
        this(groupMembershipId, Instant.now());
    }
}
