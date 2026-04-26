package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberJpaEntity;
import com.alertmns.shared.OrganisationId;

public final class MemberPersistenceMapper {

    private MemberPersistenceMapper() {}

    public static Member toDomain(MemberJpaEntity entity) {
        return Member.reconstitute(
                MemberId.from(entity.getId()),
                OrganisationId.from(entity.getOrganisationId()),
                entity.getUserId(),
                entity.getRole(),
                entity.getStatus(),
                entity.getJoinedAt()
        );
    }

    public static MemberJpaEntity toEntity(Member domain) {
        return new MemberJpaEntity(
                domain.id().value(),
                domain.organisationId().value(),
                domain.userId(),
                domain.role(),
                domain.status(),
                domain.joinedAt()
        );
    }
}
