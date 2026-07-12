package com.alertmns.alerting.infrastructure.config;

import com.alertmns.alerting.application.BroadcastAlertService;
import com.alertmns.alerting.application.ListMyAlertsService;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.alerting.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.alerting.infrastructure.adapter.outgoing.acl.GroupMembershipPortAdapter;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.AlertJpaRepository;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.AlertPersistenceAdapter;
import com.alertmns.organisation.application.CurrentMemberResolver;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.shared.EventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AlertingBeanConfig {

    // --- Ports sortants ---

    @Bean
    public AlertRepository alertRepository(AlertJpaRepository jpaRepository) {
        return new AlertPersistenceAdapter(jpaRepository);
    }

    @Bean
    public GroupMembershipPort alertingGroupMembershipPort(GroupMembershipRepository groupMembershipRepository) {
        return new GroupMembershipPortAdapter(groupMembershipRepository);
    }

    // --- Services ---

    @Bean
    public BroadcastAlertService broadcastAlertService(
            CurrentMemberResolver currentMemberResolver,
            AlertRepository alertRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new BroadcastAlertService(currentMemberResolver, alertRepository, publisher, clock);
    }

    @Bean
    public ListMyAlertsService listMyAlertsService(
            CurrentMemberResolver currentMemberResolver,
            AlertRepository alertRepository,
            GroupMembershipPort alertingGroupMembershipPort
    ) {
        return new ListMyAlertsService(currentMemberResolver, alertRepository, alertingGroupMembershipPort);
    }
}
