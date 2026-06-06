package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupKind;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant la création d'un groupe dans une organisation.
 */
public record GroupCreated(
        OrganisationId organisationId,
        GroupId groupId,
        GroupName name,
        GroupKind kind,
        Instant occurredOn
) implements DomainEvent {}
