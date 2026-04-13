package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant la création d'un groupe dans une organisation.
 */
public final class GroupCreated implements DomainEvent {

    private final GroupId groupId;
    private final GroupName name;
    private final OrganisationId organisationId;
    private final Instant occurredOn;

    public GroupCreated(GroupId groupId, GroupName name, OrganisationId organisationId) {
        this.groupId = groupId;
        this.name = name;
        this.organisationId = organisationId;
        occurredOn = Instant.now();
    }

    public GroupId groupId() {
        return groupId;
    }

    public GroupName name() {
        return name;
    }

    public OrganisationId organisationId() {
        return organisationId;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GroupCreated that = (GroupCreated) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(organisationId, that.organisationId) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, name, organisationId, occurredOn);
    }

    @Override
    public String toString() {
        return "GroupCreated{" +
                "groupId=" + groupId +
                ", name=" + name +
                ", organisationId=" + organisationId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
