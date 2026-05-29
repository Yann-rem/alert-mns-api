package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MembershipInvitationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
        name = "membership_invitations",
        indexes = {
                @Index(name = "idx_membership_invitations_email_status",
                        columnList = "invited_email, status")
        }
)
public class MembershipInvitationJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID organisationId;

    @Column(nullable = false, length = 254)
    private String invitedEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipInvitationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;
}
