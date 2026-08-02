package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, UUID> {

    Optional<MemberJpaEntity> findByUserId(UUID userId);

    boolean existsByOrganisationIdAndUserId(UUID organisationId, UUID userId);

    long countByOrganisationIdAndRoleAndStatus(UUID organisationId, MemberRole role, MemberStatus status);

    @Query("select m.userId from MemberJpaEntity m where m.organisationId = :organisationId")
    List<UUID> findUserIdsByOrganisationId(@Param("organisationId") UUID organisationId);

    @Query("""
            select m.userId from MemberJpaEntity m
            where m.id in (select gm.memberId from GroupMembershipJpaEntity gm where gm.groupId = :groupId)
            """)
    List<UUID> findUserIdsByGroupId(@Param("groupId") UUID groupId);

    @Query("select m.userId from MemberJpaEntity m where m.id in :memberIds")
    List<UUID> findUserIdsByIdIn(@Param("memberIds") Collection<UUID> memberIds);
}
