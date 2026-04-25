package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.Organisation;
import com.alertmns.organisation.domain.model.OrganisationName;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper.OrganisationPersistenceMapper;
import com.alertmns.shared.OrganisationId;

import java.util.Optional;

public final class OrganisationPersistenceAdapter implements OrganisationRepository {

    private final OrganisationJpaRepository jpaRepository;

    public OrganisationPersistenceAdapter(OrganisationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Organisation organisation) {
        jpaRepository.save(OrganisationPersistenceMapper.toEntity(organisation));
    }

    @Override
    public Optional<Organisation> findById(OrganisationId id) {
        return jpaRepository
                .findById(id.value())
                .map(OrganisationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Organisation> findByName(OrganisationName name) {
        return jpaRepository
                .findByName(name.value())
                .map(OrganisationPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByName(OrganisationName name) {
        return jpaRepository.existsByName(name.value());
    }
}
