package com.alertmns.messaging.infrastructure.adapter.outgoing.acl;

import com.alertmns.messaging.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;

import java.util.List;
import java.util.UUID;

/**
 * Adapter d'anti-corruption traduisant les besoins de Messaging sur l'appartenance aux groupes (« ce membre est-il
 * dans ce groupe ? », « quels sont ses groupes ? ») vers le port du BC Organisation.
 */
public final class GroupMembershipPortAdapter implements GroupMembershipPort {

    private final GroupMembershipRepository groupMembershipRepository;

    public GroupMembershipPortAdapter(GroupMembershipRepository groupMembershipRepository) {
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @Override
    public boolean isMember(UUID groupId, UUID memberId) {
        return groupMembershipRepository
                .findByGroupIdAndMemberId(GroupId.from(groupId), MemberId.from(memberId))
                .isPresent();
    }

    @Override
    public List<UUID> groupIdsOf(UUID memberId) {
        return groupMembershipRepository.findByMemberId(MemberId.from(memberId))
                .stream()
                .map(membership -> membership.groupId().value())
                .toList();
    }
}
