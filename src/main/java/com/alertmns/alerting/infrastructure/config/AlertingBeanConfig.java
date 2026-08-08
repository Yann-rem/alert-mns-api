package com.alertmns.alerting.infrastructure.config;

import com.alertmns.alerting.application.BroadcastAlertService;
import com.alertmns.alerting.application.DispatchAlertService;
import com.alertmns.alerting.application.ListMyAlertsService;
import com.alertmns.alerting.domain.port.outgoing.AlertRealtimePort;
import com.alertmns.alerting.domain.port.outgoing.AlertRecipientPort;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.alerting.domain.port.outgoing.GroupDirectoryPort;
import com.alertmns.alerting.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.alerting.domain.port.outgoing.IssuerDirectoryPort;
import com.alertmns.alerting.infrastructure.adapter.incoming.event.PushAlertOnBroadcastListener;
import com.alertmns.alerting.infrastructure.adapter.outgoing.acl.AlertRecipientPortAdapter;
import com.alertmns.alerting.infrastructure.adapter.outgoing.acl.GroupDirectoryPortAdapter;
import com.alertmns.alerting.infrastructure.adapter.outgoing.acl.GroupMembershipPortAdapter;
import com.alertmns.alerting.infrastructure.adapter.outgoing.acl.IssuerDirectoryPortAdapter;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.AlertJpaRepository;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.AlertPersistenceAdapter;
import com.alertmns.alerting.infrastructure.adapter.outgoing.realtime.AlertRealtimeAdapter;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.application.CurrentMemberResolver;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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

    @Bean
    public AlertRecipientPort alertRecipientPort(MemberRepository memberRepository) {
        return new AlertRecipientPortAdapter(memberRepository);
    }

    @Bean
    public AlertRealtimePort alertRealtimePort(SimpMessagingTemplate messagingTemplate) {
        return new AlertRealtimeAdapter(messagingTemplate);
    }

    @Bean
    public IssuerDirectoryPort issuerDirectoryPort(
            MemberRepository memberRepository,
            UserRepository userRepository
    ) {
        return new IssuerDirectoryPortAdapter(memberRepository, userRepository);
    }

    @Bean
    public GroupDirectoryPort alertingGroupDirectoryPort(GroupRepository groupRepository) {
        return new GroupDirectoryPortAdapter(groupRepository);
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
            GroupMembershipPort alertingGroupMembershipPort,
            IssuerDirectoryPort issuerDirectoryPort,
            GroupDirectoryPort alertingGroupDirectoryPort
    ) {
        return new ListMyAlertsService(
                currentMemberResolver,
                alertRepository,
                alertingGroupMembershipPort,
                issuerDirectoryPort,
                alertingGroupDirectoryPort
        );
    }

    /**
     * Partage volontairement les mêmes annuaires que la lecture : un membre anonymisé doit s'afficher de la même
     * façon qu'on reçoive l'alerte en direct ou qu'on la relise.
     */
    @Bean
    public DispatchAlertService dispatchAlertService(
            AlertRecipientPort alertRecipientPort,
            AlertRealtimePort alertRealtimePort,
            IssuerDirectoryPort issuerDirectoryPort,
            GroupDirectoryPort alertingGroupDirectoryPort
    ) {
        return new DispatchAlertService(
                alertRecipientPort,
                alertRealtimePort,
                issuerDirectoryPort,
                alertingGroupDirectoryPort
        );
    }

    // --- Event listeners ---

    @Bean
    public PushAlertOnBroadcastListener pushAlertOnBroadcastListener(DispatchAlertService dispatchAlertService) {
        return new PushAlertOnBroadcastListener(dispatchAlertService);
    }
}
