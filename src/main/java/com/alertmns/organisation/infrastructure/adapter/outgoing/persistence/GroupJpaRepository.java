package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, UUID> {

    boolean existsByOrganisationIdAndName(UUID organisationId, String name);
}
