package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, UUID> {

    Optional<MemberJpaEntity> findByUserId(UUID userId);

    boolean existsByOrganisationIdAndUserId(UUID organisationId, UUID userId);

    long countByOrganisationIdAndRoleAndStatus(UUID organisationId, MemberRole role, MemberStatus status);
}
