package com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.organisation.domain.port.incoming.command.AddMemberToGroupCommand;
import com.alertmns.organisation.domain.port.incoming.command.RemoveMemberFromGroupCommand;

import java.util.UUID;

public final class GroupMembershipWebMapper {

    private GroupMembershipWebMapper() {}

    public static AddMemberToGroupCommand toAddMemberToGroupCommand(UUID organisationId, UUID groupId, UUID memberId) {
        return new AddMemberToGroupCommand(organisationId.toString(), groupId.toString(), memberId.toString());
    }

    public static RemoveMemberFromGroupCommand toRemoveMemberFromGroupCommand(
            UUID organisationId,
            UUID groupId,
            UUID memberId
    ) {
        return new RemoveMemberFromGroupCommand(organisationId.toString(), groupId.toString(), memberId.toString());
    }
}
