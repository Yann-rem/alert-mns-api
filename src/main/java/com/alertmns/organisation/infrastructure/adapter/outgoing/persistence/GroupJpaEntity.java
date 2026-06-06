package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.GroupKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "organisation_groups",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_org_name",
                columnNames = {"organisation_id", "name"}
        )
)
public class GroupJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID organisationId;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupKind kind;

    @Column(nullable = false)
    private Instant createdAt;
}
