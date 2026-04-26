package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper.GroupPersistenceMapper;
import com.alertmns.shared.OrganisationId;

import java.util.Optional;

public final class GroupPersistenceAdapter implements GroupRepository {

    private final GroupJpaRepository jpaRepository;

    public GroupPersistenceAdapter(GroupJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Group group) {
        jpaRepository.save(GroupPersistenceMapper.toEntity(group));
    }

    @Override
    public Optional<Group> findById(GroupId id) {
        return jpaRepository
                .findById(id.value())
                .map(GroupPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByOrganisationIdAndGroupName(OrganisationId organisationId, GroupName name) {
        return jpaRepository.existsByOrganisationIdAndName(organisationId.value(), name.value());
    }
}
