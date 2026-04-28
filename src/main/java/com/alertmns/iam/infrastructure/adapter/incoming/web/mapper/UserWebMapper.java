package com.alertmns.iam.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.incoming.command.UpdateAbsenceMessageCommand;
import com.alertmns.iam.domain.port.incoming.command.UpdateProfileCommand;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.RegisterUserRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.UpdateAbsenceMessageRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.UpdateProfileRequest;

import java.util.UUID;

public final class UserWebMapper {

    private UserWebMapper() {}

    public static RegisterUserCommand toCommand(RegisterUserRequest request) {
        return new RegisterUserCommand(
                request.email(),
                request.rawPassword(),
                request.firstName(),
                request.lastName(),
                request.organisationId().toString()
        );
    }

    public static UpdateProfileCommand toCommand(UUID userId, UpdateProfileRequest request) {
        return new UpdateProfileCommand(
                userId.toString(),
                request.firstName(),
                request.lastName(),
                request.avatar()
        );
    }

    public static UpdateAbsenceMessageCommand toCommand(
            UUID userId,
            UpdateAbsenceMessageRequest request
    ) {
        return new UpdateAbsenceMessageCommand(userId.toString(), request.content(), request.active());
    }
}
