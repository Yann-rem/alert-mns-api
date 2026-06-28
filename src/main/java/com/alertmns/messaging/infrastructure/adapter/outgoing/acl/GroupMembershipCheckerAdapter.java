package com.alertmns.messaging.infrastructure.adapter.outgoing.acl;

import com.alertmns.messaging.domain.port.outgoing.GroupMembershipChecker;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;

import java.util.UUID;

/**
 * Adapter d'anti-corruption traduisant le besoin de Messaging (« ce membre appartient-il à ce groupe ? ») vers le
 * port du BC Organisation.
 */
public final class GroupMembershipCheckerAdapter implements GroupMembershipChecker {

    private final GroupMembershipRepository groupMembershipRepository;

    public GroupMembershipCheckerAdapter(GroupMembershipRepository groupMembershipRepository) {
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @Override
    public boolean isMember(UUID groupId, UUID memberId) {
        return groupMembershipRepository
                .findByGroupIdAndMemberId(GroupId.from(groupId), MemberId.from(memberId))
                .isPresent();
    }
}
