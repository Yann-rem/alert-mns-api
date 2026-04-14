package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.GroupMembershipId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant le retrait d'un membre d'un groupe.
 */
public final class MemberRemovedFromGroup implements DomainEvent {

    private final GroupMembershipId groupMembershipId;
    private final Instant occurredOn;

    public MemberRemovedFromGroup(GroupMembershipId groupMembershipId) {
        this.groupMembershipId = groupMembershipId;
        occurredOn = Instant.now();
    }

    public GroupMembershipId groupMembershipId() {
        return groupMembershipId;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MemberRemovedFromGroup that = (MemberRemovedFromGroup) o;
        return Objects.equals(groupMembershipId, that.groupMembershipId) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupMembershipId, occurredOn);
    }

    @Override
    public String toString() {
        return "MemberRemovedFromGroup{" +
                "groupMembershipId=" + groupMembershipId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
