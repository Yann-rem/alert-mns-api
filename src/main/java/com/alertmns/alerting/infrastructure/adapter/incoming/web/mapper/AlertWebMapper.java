package com.alertmns.alerting.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertAudience;
import com.alertmns.alerting.domain.port.incoming.command.BroadcastAlertCommand;
import com.alertmns.alerting.infrastructure.adapter.incoming.web.dto.AlertResponse;
import com.alertmns.alerting.infrastructure.adapter.incoming.web.dto.BroadcastAlertRequest;

public final class AlertWebMapper {

    private AlertWebMapper() {}

    public static BroadcastAlertCommand toBroadcastAlertCommand(BroadcastAlertRequest request) {
        return new BroadcastAlertCommand(
                request.content(),
                request.level(),
                request.audienceKind(),
                request.groupId()
        );
    }

    public static AlertResponse toAlertResponse(Alert alert) {
        AlertAudience audience = alert.audience();
        return new AlertResponse(
                alert.id().value(),
                alert.issuerId(),
                audience.kind(),
                audience.groupId(),
                alert.level(),
                alert.content().value(),
                alert.issuedAt()
        );
    }
}
