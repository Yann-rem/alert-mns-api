package com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertAudience;
import com.alertmns.alerting.domain.model.AlertContent;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.AlertJpaEntity;
import com.alertmns.shared.OrganisationId;

public final class AlertPersistenceMapper {

    private AlertPersistenceMapper() {}

    public static Alert toDomain(AlertJpaEntity entity) {
        AlertAudience audience = switch (entity.getAudienceKind()) {
            case ORGANISATION -> AlertAudience.organisation();
            case GROUP -> AlertAudience.group(entity.getGroupId());
        };
        return Alert.reconstitute(
                AlertId.from(entity.getId()),
                OrganisationId.from(entity.getOrganisationId()),
                entity.getIssuerId(),
                AlertContent.of(entity.getContent()),
                audience,
                entity.getLevel(),
                entity.getIssuedAt()
        );
    }

    public static AlertJpaEntity toEntity(Alert domain) {
        AlertAudience audience = domain.audience();
        return new AlertJpaEntity(
                domain.id().value(),
                domain.organisationId().value(),
                domain.issuerId(),
                domain.content().value(),
                audience.kind(),
                audience.groupId(),
                domain.level(),
                domain.issuedAt()
        );
    }
}
