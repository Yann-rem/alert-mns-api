package com.alertmns.identity.infrastructure.adapter.outgoing.persistence;

import com.alertmns.identity.domain.model.Email;
import com.alertmns.identity.domain.model.User;
import com.alertmns.shared.UserId;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.mapper.UserPersistenceMapper;

import java.util.Optional;

public final class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserPersistenceAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(User user) {
        jpaRepository.save(UserPersistenceMapper.toEntity(user));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository
                .findById(id.value())
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository
                .findByEmail(email.value())
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }
}
