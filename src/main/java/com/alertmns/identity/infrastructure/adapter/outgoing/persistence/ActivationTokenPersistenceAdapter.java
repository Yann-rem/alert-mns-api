package com.alertmns.identity.infrastructure.adapter.outgoing.persistence;

import com.alertmns.identity.domain.model.ActivationToken;
import com.alertmns.identity.domain.model.HashedToken;
import com.alertmns.identity.domain.port.outgoing.ActivationTokenRepository;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.mapper.ActivationTokenPersistenceMapper;
import com.alertmns.shared.UserId;

import java.util.Optional;

public final class ActivationTokenPersistenceAdapter implements ActivationTokenRepository {

    private final ActivationTokenJpaRepository jpaRepository;

    public ActivationTokenPersistenceAdapter(ActivationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(ActivationToken token) {
        jpaRepository.save(ActivationTokenPersistenceMapper.toEntity(token));
    }

    @Override
    public Optional<ActivationToken> findByHash(HashedToken hash) {
        return jpaRepository
                .findByHash(hash.hex())
                .map(ActivationTokenPersistenceMapper::toDomain);
    }

    @Override
    public void deleteByUserId(UserId userId) {
        jpaRepository.deleteByUserId(userId.value());
    }
}
