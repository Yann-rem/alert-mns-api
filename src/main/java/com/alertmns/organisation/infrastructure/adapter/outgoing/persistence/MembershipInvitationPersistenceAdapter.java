package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.domain.model.MembershipInvitationStatus;
import com.alertmns.organisation.domain.port.outgoing.MembershipInvitationRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper.MembershipInvitationPersistenceMapper;
import com.alertmns.shared.Email;
import com.alertmns.shared.MembershipInvitationId;
import com.alertmns.shared.OrganisationId;

import java.util.List;
import java.util.Optional;

public final class MembershipInvitationPersistenceAdapter implements MembershipInvitationRepository {

    private final MembershipInvitationJpaRepository jpaRepository;

    public MembershipInvitationPersistenceAdapter(MembershipInvitationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(MembershipInvitation invitation) {
        jpaRepository.save(MembershipInvitationPersistenceMapper.toEntity(invitation));
    }

    @Override
    public Optional<MembershipInvitation> findById(MembershipInvitationId id) {
        return jpaRepository
                .findById(id.value())
                .map(MembershipInvitationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<MembershipInvitation> findPendingByEmail(Email email) {
        return jpaRepository
                .findByInvitedEmailAndStatus(email.value(), MembershipInvitationStatus.PENDING)
                .map(MembershipInvitationPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsPendingByEmail(Email email) {
        return jpaRepository.existsByInvitedEmailAndStatus(email.value(), MembershipInvitationStatus.PENDING);
    }

    @Override
    public List<MembershipInvitation> findByOrganisationIdAndStatus(
            OrganisationId organisationId, MembershipInvitationStatus status) {
        return jpaRepository
                .findByOrganisationIdAndStatusOrderByCreatedAtDesc(organisationId.value(), status)
                .stream()
                .map(MembershipInvitationPersistenceMapper::toDomain)
                .toList();
    }
}
