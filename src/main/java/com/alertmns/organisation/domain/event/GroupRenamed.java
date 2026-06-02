package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant le renommage d'un groupe.
 */
public record GroupRenamed(
        OrganisationId organisationId,
        GroupId groupId,
        GroupName name,
        Instant occurredOn
) implements DomainEvent {}
