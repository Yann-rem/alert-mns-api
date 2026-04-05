package com.alertmns.iam.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.incoming.command.UpdateAbsenceMessageCommand;
import com.alertmns.iam.domain.port.incoming.command.UpdateProfileCommand;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.RegisterUserRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.UpdateAbsenceMessageRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.UpdateProfileRequest;

public class UserWebMapper {

    private UserWebMapper() {}

    public static RegisterUserCommand toCommand(RegisterUserRequest request) {
        return new RegisterUserCommand(
                request.email(),
                request.rawPassword(),
                request.firstName(),
                request.lastName()
        );
    }

    public static UpdateProfileCommand toCommand(String userId, UpdateProfileRequest request) {
        return new UpdateProfileCommand(
                userId,
                request.firstName(),
                request.lastName(),
                request.avatar()
        );
    }

    public static UpdateAbsenceMessageCommand toCommand(
            String userId,
            UpdateAbsenceMessageRequest request
    ) {
        return new UpdateAbsenceMessageCommand(userId, request.content(), request.active());
    }
}
