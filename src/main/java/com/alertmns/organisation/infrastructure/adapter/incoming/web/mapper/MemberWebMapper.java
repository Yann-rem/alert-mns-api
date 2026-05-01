package com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.organisation.domain.port.incoming.command.InviteMemberCommand;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.InviteMemberRequest;

import java.util.UUID;

public final class MemberWebMapper {

    private MemberWebMapper() {}

    public static InviteMemberCommand toInviteMemberCommand(UUID organisationId, InviteMemberRequest request) {
        return new InviteMemberCommand(
                organisationId.toString(),
                request.userId().toString(),
                request.role().name()
        );
    }
}
