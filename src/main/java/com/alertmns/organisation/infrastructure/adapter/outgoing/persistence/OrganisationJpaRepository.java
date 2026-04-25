package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganisationJpaRepository extends JpaRepository<OrganisationJpaEntity, UUID> {

    Optional<OrganisationJpaEntity> findByName(String name);

    boolean existsByName(String name);
}
