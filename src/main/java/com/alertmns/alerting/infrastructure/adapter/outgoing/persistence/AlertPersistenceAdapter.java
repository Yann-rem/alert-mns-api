package com.alertmns.alerting.infrastructure.adapter.outgoing.persistence;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertAudienceKind;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.mapper.AlertPersistenceMapper;
import com.alertmns.shared.OrganisationId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class AlertPersistenceAdapter implements AlertRepository {

    private final AlertJpaRepository jpaRepository;

    public AlertPersistenceAdapter(AlertJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Alert alert) {
        jpaRepository.save(AlertPersistenceMapper.toEntity(alert));
    }

    @Override
    public Optional<Alert> findById(AlertId id) {
        return jpaRepository.findById(id.value()).map(AlertPersistenceMapper::toDomain);
    }

    @Override
    public List<Alert> findOrganisationWide(OrganisationId organisationId) {
        return jpaRepository
                .findByOrganisationIdAndAudienceKind(organisationId.value(), AlertAudienceKind.ORGANISATION)
                .stream()
                .map(AlertPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Alert> findByGroupIdIn(Collection<UUID> groupIds) {
        return jpaRepository.findByGroupIdIn(groupIds)
                .stream()
                .map(AlertPersistenceMapper::toDomain)
                .toList();
    }
}
