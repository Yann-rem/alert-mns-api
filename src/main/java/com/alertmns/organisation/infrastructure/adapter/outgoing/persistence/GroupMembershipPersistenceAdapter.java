package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper.GroupMembershipPersistenceMapper;

import java.util.List;
import java.util.Optional;

public final class GroupMembershipPersistenceAdapter implements GroupMembershipRepository {

    private final GroupMembershipJpaRepository jpaRepository;

    public GroupMembershipPersistenceAdapter(GroupMembershipJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(GroupMembership groupMembership) {
        jpaRepository.save(GroupMembershipPersistenceMapper.toEntity(groupMembership));
    }

    @Override
    public void delete(GroupMembership groupMembership) {
        jpaRepository.delete(GroupMembershipPersistenceMapper.toEntity(groupMembership));
    }

    @Override
    public Optional<GroupMembership> findByGroupIdAndMemberId(GroupId groupId, MemberId memberId) {
        return jpaRepository
                .findByGroupIdAndMemberId(groupId.value(), memberId.value())
                .map(GroupMembershipPersistenceMapper::toDomain);
    }

    @Override
    public List<GroupMembership> findByMemberId(MemberId memberId) {
        return jpaRepository.findByMemberId(memberId.value())
                .stream()
                .map(GroupMembershipPersistenceMapper::toDomain)
                .toList();
    }
}
