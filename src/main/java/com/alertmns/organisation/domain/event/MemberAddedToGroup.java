package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.GroupMembershipId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;

/**
 * Événement de domaine représentant l'ajout d'un membre à un groupe.
 */
public record MemberAddedToGroup(GroupMembershipId groupMembershipId, Instant occurredOn) implements DomainEvent {

    public MemberAddedToGroup(GroupMembershipId groupMembershipId) {
        this(groupMembershipId, Instant.now());
    }
}
