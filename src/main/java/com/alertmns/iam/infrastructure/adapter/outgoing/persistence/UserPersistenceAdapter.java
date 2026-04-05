package com.alertmns.iam.infrastructure.adapter.outgoing.persistence;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.mapper.UserPersistenceMapper;

import java.util.Optional;

public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserPersistenceAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public void save(User user) {
        userJpaRepository.save(UserPersistenceMapper.toEntity(user));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository
                .findById(id.value())
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userJpaRepository
                .findByEmail(email.value())
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.value());
    }
}
