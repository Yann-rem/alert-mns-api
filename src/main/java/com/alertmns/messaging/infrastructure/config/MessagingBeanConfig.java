package com.alertmns.messaging.infrastructure.config;

import com.alertmns.messaging.application.CreateConversationFromGroupService;
import com.alertmns.messaging.application.CreateDirectConversationService;
import com.alertmns.messaging.application.RenameConversationService;
import com.alertmns.messaging.domain.port.incoming.CreateConversationFromGroupUseCase;
import com.alertmns.messaging.domain.port.incoming.RenameConversationUseCase;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.infrastructure.adapter.incoming.event.CreateConversationOnGroupCreatedListener;
import com.alertmns.messaging.infrastructure.adapter.incoming.event.RenameConversationOnGroupRenamedListener;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationPersistenceAdapter;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.EventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class MessagingBeanConfig {

    // --- Ports sortants ---

    @Bean
    public ConversationRepository conversationRepository(ConversationJpaRepository jpaRepository) {
        return new ConversationPersistenceAdapter(jpaRepository);
    }

    // --- Services ---

    @Bean
    public CreateConversationFromGroupService createConversationFromGroupService(
            ConversationRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new CreateConversationFromGroupService(repository, publisher, clock);
    }

    @Bean
    public RenameConversationService renameConversationService(
            ConversationRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new RenameConversationService(repository, publisher, clock);
    }

    @Bean
    public CreateDirectConversationService createDirectConversationService(
            CurrentUserPort currentUserPort,
            MemberRepository memberRepository,
            ConversationRepository conversationRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new CreateDirectConversationService(
                currentUserPort,
                memberRepository,
                conversationRepository,
                publisher,
                clock
        );
    }

    // --- Event listeners ---

    @Bean
    public CreateConversationOnGroupCreatedListener createConversationOnGroupCreatedListener(
            CreateConversationFromGroupUseCase createConversationFromGroupUseCase) {
        return new CreateConversationOnGroupCreatedListener(createConversationFromGroupUseCase);
    }

    @Bean
    public RenameConversationOnGroupRenamedListener renameConversationOnGroupRenamedListener(
            RenameConversationUseCase renameConversationUseCase) {
        return new RenameConversationOnGroupRenamedListener(renameConversationUseCase);
    }
}
