package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import org.springframework.data.domain.Pageable;
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

    /**
     * Liste filtrée des membres. Chaque filtre est neutralisé quand son paramètre vaut
     * {@code null}, ce qui évite de construire la requête dynamiquement.
     *
     * <p>Tri sur {@code joinedAt} puis {@code id} : {@code joinedAt} seul ne suffit pas à garantir
     * un ordre stable entre deux pages si plusieurs membres rejoignent au même instant.</p>
     */
    @Query("""
            select m from MemberJpaEntity m
            where m.organisationId = :organisationId
              and (:status is null or m.status = :status)
              and (:role is null or m.role = :role)
              and (:userIds is null or m.userId in :userIds)
              and (:groupId is null
                   or m.id in (select gm.memberId from GroupMembershipJpaEntity gm
                               where gm.groupId = :groupId))
            order by m.joinedAt asc, m.id asc
            """)
    List<MemberJpaEntity> findFiltered(
            @Param("organisationId") UUID organisationId,
            @Param("status") MemberStatus status,
            @Param("role") MemberRole role,
            @Param("userIds") Collection<UUID> userIds,
            @Param("groupId") UUID groupId,
            Pageable pageable);

    /** Compte les membres correspondant aux mêmes filtres que {@link #findFiltered}. */
    @Query("""
            select count(m) from MemberJpaEntity m
            where m.organisationId = :organisationId
              and (:status is null or m.status = :status)
              and (:role is null or m.role = :role)
              and (:userIds is null or m.userId in :userIds)
              and (:groupId is null
                   or m.id in (select gm.memberId from GroupMembershipJpaEntity gm
                               where gm.groupId = :groupId))
            """)
    long countFiltered(
            @Param("organisationId") UUID organisationId,
            @Param("status") MemberStatus status,
            @Param("role") MemberRole role,
            @Param("userIds") Collection<UUID> userIds,
            @Param("groupId") UUID groupId);
}
