package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.GroupMembershipId;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupMembershipJpaEntity;
import com.alertmns.shared.OrganisationId;

public final class GroupMembershipPersistenceMapper {

    private GroupMembershipPersistenceMapper() {}

    public static GroupMembership toDomain(GroupMembershipJpaEntity entity) {
        return GroupMembership.reconstitute(
                GroupMembershipId.from(entity.getId()),
                OrganisationId.from(entity.getOrganisationId()),
                GroupId.from(entity.getGroupId()),
                MemberId.from(entity.getMemberId()),
                entity.getJoinedAt()
        );
    }

    public static GroupMembershipJpaEntity toEntity(GroupMembership domain) {
        return new GroupMembershipJpaEntity(
                domain.id().value(),
                domain.organisationId().value(),
                domain.groupId().value(),
                domain.memberId().value(),
                domain.joinedAt()
        );
    }
}
