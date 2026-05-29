package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MembershipInvitationJpaEntity;
import com.alertmns.shared.Email;
import com.alertmns.shared.MembershipInvitationId;
import com.alertmns.shared.OrganisationId;

public final class MembershipInvitationPersistenceMapper {

    private MembershipInvitationPersistenceMapper() {}

    public static MembershipInvitation toDomain(MembershipInvitationJpaEntity entity) {
        return MembershipInvitation.reconstitute(
                MembershipInvitationId.from(entity.getId()),
                OrganisationId.from(entity.getOrganisationId()),
                Email.of(entity.getInvitedEmail()),
                entity.getRole(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt()
        );
    }

    public static MembershipInvitationJpaEntity toEntity(MembershipInvitation domain) {
        return new MembershipInvitationJpaEntity(
                domain.id().value(),
                domain.organisationId().value(),
                domain.invitedEmail().value(),
                domain.role(),
                domain.status(),
                domain.createdAt(),
                domain.expiresAt()
        );
    }
}
