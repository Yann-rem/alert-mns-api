package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.MemberRemovedFromGroup;
import com.alertmns.organisation.domain.exception.GroupMembershipNotFoundException;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.incoming.RemoveMemberFromGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.RemoveMemberFromGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.shared.EventPublisher;

import java.util.List;
import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration du retrait d'un membre d'un groupe.
 *
 * <p>Charge l'adhésion → supprime → publie l'événement {@link MemberRemovedFromGroup}.</p>
 *
 * <p>L'événement est instancié directement par ce service — l'agrégat
 * {@link GroupMembership} étant supprimé, il ne peut pas publier d'événement via le
 * mécanisme habituel de {@code pullDomainEvents()}.</p>
 */
public final class RemoveMemberFromGroupService implements RemoveMemberFromGroupUseCase {

    private final GroupMembershipRepository groupMembershipRepository;
    private final EventPublisher publisher;

    public RemoveMemberFromGroupService(
            GroupMembershipRepository groupMembershipRepository,
            EventPublisher publisher
    ) {
        this.groupMembershipRepository = Objects.requireNonNull(
                groupMembershipRepository,
                "groupMembershipRepository must not be null"
        );

        this.publisher = Objects.requireNonNull(
                publisher,
                "publisher must not be null"
        );
    }

    @Override
    public void remove(RemoveMemberFromGroupCommand command) {
        GroupId groupId = GroupId.from(command.groupId());
        MemberId memberId = MemberId.from(command.memberId());

        GroupMembership membership = groupMembershipRepository
                .findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> new GroupMembershipNotFoundException(groupId, memberId));

        groupMembershipRepository.delete(membership);
        publisher.publish(List.of(new MemberRemovedFromGroup(membership.id())));
    }
}
