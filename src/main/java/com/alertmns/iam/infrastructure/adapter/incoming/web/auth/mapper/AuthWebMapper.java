package com.alertmns.iam.infrastructure.adapter.incoming.web.auth.mapper;

import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.incoming.result.ActivationTokenContext;
import com.alertmns.iam.infrastructure.adapter.incoming.web.auth.dto.MeResponse;
import com.alertmns.iam.infrastructure.adapter.incoming.web.auth.dto.ValidateMagicLinkResponse;

public final class AuthWebMapper {

    private AuthWebMapper() {}

    public static MeResponse toMeResponse(User user) {
        return new MeResponse(
                user.id().value().toString(),
                user.organisationId().value().toString(),
                user.email().value(),
                user.profile().firstName().value(),
                user.profile().lastName().value(),
                user.role().name()
        );
    }

    public static ValidateMagicLinkResponse toValidateMagicLinkResponse(ActivationTokenContext context) {
        return new ValidateMagicLinkResponse(
                context.email().value(),
                context.firstName().value(),
                context.lastName().value()
        );
    }
}
