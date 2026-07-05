package com.alertmns.alerting.infrastructure.config;

import com.alertmns.alerting.application.BroadcastAlertService;
import com.alertmns.alerting.application.CurrentMemberResolver;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.AlertJpaRepository;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.AlertPersistenceAdapter;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.CurrentUserPort;
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

    // --- Services ---

    @Bean
    public CurrentMemberResolver alertingCurrentMemberResolver(
            CurrentUserPort currentUserPort,
            MemberRepository memberRepository
    ) {
        return new CurrentMemberResolver(currentUserPort, memberRepository);
    }

    @Bean
    public BroadcastAlertService broadcastAlertService(
            CurrentMemberResolver alertingCurrentMemberResolver,
            AlertRepository alertRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new BroadcastAlertService(alertingCurrentMemberResolver, alertRepository, publisher, clock);
    }
}
