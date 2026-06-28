package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, UUID> {}
