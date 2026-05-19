package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper.MemberPersistenceMapper;
import com.alertmns.shared.OrganisationId;

import java.util.Optional;
import java.util.UUID;

public final class MemberPersistenceAdapter implements MemberRepository {

    private final MemberJpaRepository jpaRepository;

    public MemberPersistenceAdapter(MemberJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Member member) {
        jpaRepository.save(MemberPersistenceMapper.toEntity(member));
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        return jpaRepository
                .findById(id.value())
                .map(MemberPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Member> findByUserId(UUID userId) {
        return jpaRepository
                .findByUserId(userId)
                .map(MemberPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByOrganisationIdAndUserId(OrganisationId organisationId, UUID userId) {
        return jpaRepository.existsByOrganisationIdAndUserId(organisationId.value(), userId);
    }
}
