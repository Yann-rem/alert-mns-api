package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupJpaEntity;
import com.alertmns.shared.OrganisationId;

public final class GroupPersistenceMapper {

    private GroupPersistenceMapper() {}

    public static Group toDomain(GroupJpaEntity entity) {
        return Group.reconstitute(
                GroupId.from(entity.getId()),
                OrganisationId.from(entity.getOrganisationId()),
                GroupName.of(entity.getName()),
                entity.getKind(),
                entity.getCreatedAt()
        );
    }

    public static GroupJpaEntity toEntity(Group domain) {
        return new GroupJpaEntity(
                domain.id().value(),
                domain.organisationId().value(),
                domain.name().value(),
                domain.kind(),
                domain.createdAt()
        );
    }
}
