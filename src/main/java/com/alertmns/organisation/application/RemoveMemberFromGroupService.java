package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.MemberRemovedFromGroup;
import com.alertmns.organisation.domain.exception.GroupMembershipNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.incoming.RemoveMemberFromGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.RemoveMemberFromGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration du retrait d'un membre d'un groupe.
 *
 * <p>Parse (VOs) → load (adhésion) → check (organisation match) → act (delete)
 * → publish l'événement {@link MemberRemovedFromGroup}.</p>
 *
 * <p>L'événement est instancié directement par ce service — l'agrégat
 * {@link GroupMembership} étant supprimé, il ne peut pas publier d'événement via le
 * mécanisme habituel de {@code pullDomainEvents()}.</p>
 */
public final class RemoveMemberFromGroupService implements RemoveMemberFromGroupUseCase {

    private final GroupMembershipRepository groupMembershipRepository;
    private final EventPublisher publisher;
    private final Clock clock;

    public RemoveMemberFromGroupService(
            GroupMembershipRepository groupMembershipRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        this.groupMembershipRepository = Objects.requireNonNull(
                groupMembershipRepository,
                "groupMembershipRepository must not be null"
        );
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void remove(RemoveMemberFromGroupCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        GroupId groupId = GroupId.from(command.groupId());
        MemberId memberId = MemberId.from(command.memberId());

        GroupMembership membership = groupMembershipRepository
                .findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> new GroupMembershipNotFoundException(groupId, memberId));

        if (!organisationId.equals(membership.organisationId())) {
            throw new OrganisationMismatchException(membership.organisationId(), organisationId);
        }

        groupMembershipRepository.delete(membership);
        publisher.publish(List.of(new MemberRemovedFromGroup(organisationId, membership.id(), clock.instant())));
    }
}
