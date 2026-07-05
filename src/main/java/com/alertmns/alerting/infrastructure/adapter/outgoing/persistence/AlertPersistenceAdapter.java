package com.alertmns.alerting.infrastructure.adapter.outgoing.persistence;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.mapper.AlertPersistenceMapper;

import java.util.Optional;

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
}
