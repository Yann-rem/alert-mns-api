package com.alertmns.identity.infrastructure.adapter.incoming.web.auth.mapper;

import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.result.ActivationTokenContext;
import com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto.MeResponse;
import com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto.ValidateMagicLinkResponse;

public final class AuthWebMapper {

    private AuthWebMapper() {}

    public static MeResponse toMeResponse(User user) {
        return new MeResponse(
                user.id().value().toString(),
                user.email().value(),
                user.profile().firstName().value(),
                user.profile().lastName().value()
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
