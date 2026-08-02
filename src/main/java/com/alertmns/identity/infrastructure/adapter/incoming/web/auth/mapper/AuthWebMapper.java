package com.alertmns.identity.infrastructure.adapter.incoming.web.auth.mapper;

import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.result.ActivationTokenContext;
import com.alertmns.identity.domain.port.outgoing.UserMembershipProvider.Membership;
import com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto.MeResponse;
import com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto.MeResponse.AbsenceMessageResponse;
import com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto.ValidateMagicLinkResponse;

import java.util.Optional;

public final class AuthWebMapper {

    private AuthWebMapper() {}

    /**
     * @param user       l'utilisateur connecté
     * @param membership son adhésion, vide s'il n'est membre d'aucune organisation
     */
    public static MeResponse toMeResponse(User user, Optional<Membership> membership) {
        return new MeResponse(
                user.id().value().toString(),
                user.email().value(),
                user.profile().firstName().value(),
                user.profile().lastName().value(),
                membership.map(m -> m.memberId().toString()).orElse(null),
                membership.map(m -> m.organisationId().toString()).orElse(null),
                membership.map(Membership::role).orElse(null),
                membership.map(Membership::status).orElse(null),
                user.profile().absenceMessage()
                        .map(absence -> new AbsenceMessageResponse(absence.content(), absence.active()))
                        .orElse(null)
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
