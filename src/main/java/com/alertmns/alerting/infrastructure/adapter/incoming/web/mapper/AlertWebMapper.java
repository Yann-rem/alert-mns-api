package com.alertmns.alerting.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.alerting.domain.port.incoming.command.BroadcastAlertCommand;
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
}
