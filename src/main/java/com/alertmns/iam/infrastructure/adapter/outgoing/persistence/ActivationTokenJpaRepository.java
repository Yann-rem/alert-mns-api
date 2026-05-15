package com.alertmns.iam.infrastructure.adapter.outgoing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface ActivationTokenJpaRepository extends JpaRepository<ActivationTokenJpaEntity, UUID> {

    Optional<ActivationTokenJpaEntity> findByHash(String hash);

    @Transactional
    void deleteByUserId(UUID userId);
}
