package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.MembershipInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MembershipInvitationJpaRepository extends JpaRepository<MembershipInvitationJpaEntity, UUID> {

    Optional<MembershipInvitationJpaEntity> findByInvitedEmailAndStatus(
            String invitedEmail, MembershipInvitationStatus status);

    boolean existsByInvitedEmailAndStatus(String invitedEmail, MembershipInvitationStatus status);
}
