package com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.organisation.domain.port.incoming.command.CreateGroupCommand;
import com.alertmns.organisation.domain.port.incoming.command.RenameGroupCommand;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateGroupRequest;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.RenameGroupRequest;

import java.util.UUID;

public final class GroupWebMapper {

    private GroupWebMapper() {}

    public static CreateGroupCommand toCreateGroupCommand(UUID organisationId, CreateGroupRequest request) {
        return new CreateGroupCommand(organisationId.toString(), request.name());
    }

    public static RenameGroupCommand toRenameGroupCommand(
            UUID organisationId,
            UUID groupId,
            RenameGroupRequest request
    ) {
        return new RenameGroupCommand(organisationId.toString(), groupId.toString(), request.name());
    }
}
