package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.organisation.domain.model.Organisation;
import com.alertmns.organisation.domain.model.OrganisationName;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.OrganisationJpaEntity;
import com.alertmns.shared.OrganisationId;

public final class OrganisationPersistenceMapper {

    private OrganisationPersistenceMapper() {}

    public static Organisation toDomain(OrganisationJpaEntity entity) {
        return Organisation.reconstitute(
                OrganisationId.from(entity.getId()),
                OrganisationName.of(entity.getName()),
                entity.getCreatedAt()
        );
    }

    public static OrganisationJpaEntity toEntity(Organisation domain) {
        return new OrganisationJpaEntity(
                domain.id().value(),
                domain.name().value(),
                domain.createdAt()
        );
    }
}
