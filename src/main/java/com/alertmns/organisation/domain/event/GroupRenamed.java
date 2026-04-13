package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant le renommage d'un groupe.
 */
public final class GroupRenamed implements DomainEvent {

    private final GroupId groupId;
    private final GroupName name;
    private final Instant occurredOn;

    public GroupRenamed(GroupId groupId, GroupName name) {
        this.name = name;
        this.groupId = groupId;
        occurredOn = Instant.now();
    }

    public GroupId groupId() {
        return groupId;
    }

    public GroupName name() {
        return name;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GroupRenamed that = (GroupRenamed) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, name, occurredOn);
    }

    @Override
    public String toString() {
        return "GroupRenamed{" +
                "groupId=" + groupId +
                ", name=" + name +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
