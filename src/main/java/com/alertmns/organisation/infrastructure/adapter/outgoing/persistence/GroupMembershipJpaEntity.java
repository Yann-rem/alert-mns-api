package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(
        name = "group_memberships",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_membership_group_member",
                columnNames = {"group_id", "member_id"}
        )
)
public class GroupMembershipJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID organisationId;

    @Column(nullable = false)
    private UUID groupId;

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private Instant joinedAt;
}
