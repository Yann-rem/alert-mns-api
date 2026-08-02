package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.outgoing.MemberFilters;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper.MemberPersistenceMapper;
import com.alertmns.shared.OrganisationId;
import org.springframework.data.domain.PageRequest;

import java.util.Collection;
import java.util.List;
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

    @Override
    public long countByOrganisationIdAndRoleAndStatus(
            OrganisationId organisationId, MemberRole role, MemberStatus status) {
        return jpaRepository.countByOrganisationIdAndRoleAndStatus(organisationId.value(), role, status);
    }

    @Override
    public List<UUID> findUserIdsByOrganisationId(OrganisationId organisationId) {
        return jpaRepository.findUserIdsByOrganisationId(organisationId.value());
    }

    @Override
    public List<UUID> findUserIdsByGroupId(GroupId groupId) {
        return jpaRepository.findUserIdsByGroupId(groupId.value());
    }

    @Override
    public List<UUID> findUserIdsByIdIn(Collection<MemberId> memberIds) {
        return jpaRepository.findUserIdsByIdIn(memberIds.stream().map(MemberId::value).toList());
    }

    @Override
    public List<Member> findByOrganisationId(
            OrganisationId organisationId, MemberFilters filters, int page, int size) {
        return jpaRepository
                .findFiltered(
                        organisationId.value(),
                        filters.status(),
                        filters.role(),
                        filters.userIds(),
                        PageRequest.of(page, size))
                .stream()
                .map(MemberPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countByOrganisationId(OrganisationId organisationId, MemberFilters filters) {
        return jpaRepository.countFiltered(
                organisationId.value(), filters.status(), filters.role(), filters.userIds());
    }
}
