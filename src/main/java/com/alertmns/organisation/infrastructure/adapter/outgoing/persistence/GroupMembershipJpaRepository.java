package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupMembershipJpaRepository extends JpaRepository<GroupMembershipJpaEntity, UUID> {

    Optional<GroupMembershipJpaEntity> findByGroupIdAndMemberId(UUID groupId, UUID memberId);
}
