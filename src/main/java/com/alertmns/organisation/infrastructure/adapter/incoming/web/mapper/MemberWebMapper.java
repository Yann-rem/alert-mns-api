package com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.domain.port.incoming.ListMembersUseCase.MembersPage;
import com.alertmns.organisation.domain.port.incoming.ListPendingInvitationsUseCase.PendingInvitation;
import com.alertmns.organisation.domain.port.incoming.MemberDirectoryEntry;
import com.alertmns.organisation.domain.port.outgoing.UserDirectoryPort.UserSummary;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.MemberSummaryResponse;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.MembersPageResponse;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.PendingInvitationResponse;

import java.time.Instant;
import java.util.Optional;

/** Conversion des résultats de lecture du BC Organisation vers les DTO web. */
public final class MemberWebMapper {

    private MemberWebMapper() {
    }

    /** @param now instant de référence pour signaler une invitation périmée */
    public static PendingInvitationResponse toPendingInvitationResponse(
            PendingInvitation pending, Instant now) {
        MembershipInvitation invitation = pending.invitation();
        Optional<UserSummary> user = pending.user();

        return new PendingInvitationResponse(
                invitation.id().value(),
                invitation.invitedEmail().value(),
                user.map(UserSummary::firstName).orElse(null),
                user.map(UserSummary::lastName).orElse(null),
                invitation.role().name(),
                invitation.createdAt(),
                invitation.expiresAt(),
                now.isAfter(invitation.expiresAt())
        );
    }

    public static MembersPageResponse toMembersPageResponse(MembersPage page, int pageIndex, int size) {
        return new MembersPageResponse(
                page.entries().stream().map(MemberWebMapper::toMemberSummaryResponse).toList(),
                page.total(),
                pageIndex,
                size
        );
    }

    /**
     * Un membre dont l'utilisateur est introuvable est tout de même exposé, avec des champs
     * d'identité nuls — l'administration doit pouvoir constater l'anomalie plutôt que de voir
     * la ligne disparaître silencieusement.
     */
    public static MemberSummaryResponse toMemberSummaryResponse(MemberDirectoryEntry entry) {
        Member member = entry.member();
        Optional<UserSummary> user = entry.user();

        return new MemberSummaryResponse(
                member.id().value(),
                member.userId(),
                user.map(UserSummary::firstName).orElse(null),
                user.map(UserSummary::lastName).orElse(null),
                user.map(UserSummary::email).orElse(null),
                member.role().name(),
                member.status().name(),
                user.map(UserSummary::accountStatus).orElse(null),
                user.map(UserSummary::anonymized).orElse(false),
                member.joinedAt()
        );
    }
}
