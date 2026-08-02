package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.GroupKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, UUID> {

    Optional<GroupJpaEntity> findByOrganisationIdAndKind(UUID organisationId, GroupKind kind);

    boolean existsByOrganisationIdAndName(UUID organisationId, String name);

    boolean existsByOrganisationIdAndKind(UUID organisationId, GroupKind kind);
}
