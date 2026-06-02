package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine signalant qu'un nouveau membre a rejoint une organisation.
 */
public record MemberJoined(
        OrganisationId organisationId,
        MemberId memberId,
        UUID userId,
        MemberRole role,
        Instant occurredOn
) implements DomainEvent {}
