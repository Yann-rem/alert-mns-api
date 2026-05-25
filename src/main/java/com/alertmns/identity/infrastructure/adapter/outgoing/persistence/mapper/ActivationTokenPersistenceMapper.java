package com.alertmns.identity.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.identity.domain.model.ActivationToken;
import com.alertmns.identity.domain.model.ActivationTokenId;
import com.alertmns.identity.domain.model.HashedToken;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.ActivationTokenJpaEntity;
import com.alertmns.shared.UserId;

public final class ActivationTokenPersistenceMapper {

    private ActivationTokenPersistenceMapper() {}

    public static ActivationToken toDomain(ActivationTokenJpaEntity entity) {
        return ActivationToken.reconstitute(
                ActivationTokenId.from(entity.getId()),
                UserId.from(entity.getUserId()),
                HashedToken.of(entity.getHash()),
                entity.getCreatedAt(),
                entity.getExpiresAt()
        );
    }

    public static ActivationTokenJpaEntity toEntity(ActivationToken domain) {
        return new ActivationTokenJpaEntity(
                domain.id().value(),
                domain.userId().value(),
                domain.hash().hex(),
                domain.createdAt(),
                domain.expiresAt()
        );
    }
}
