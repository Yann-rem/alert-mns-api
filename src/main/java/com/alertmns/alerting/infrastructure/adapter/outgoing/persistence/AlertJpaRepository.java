package com.alertmns.alerting.infrastructure.adapter.outgoing.persistence;

import com.alertmns.alerting.domain.model.AlertAudienceKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, UUID> {

    List<AlertJpaEntity> findByOrganisationIdAndAudienceKind(UUID organisationId, AlertAudienceKind audienceKind);

    List<AlertJpaEntity> findByGroupIdIn(Collection<UUID> groupIds);
}
