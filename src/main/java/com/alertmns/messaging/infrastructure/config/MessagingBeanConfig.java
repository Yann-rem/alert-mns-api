package com.alertmns.messaging.infrastructure.config;

import com.alertmns.messaging.application.CreateConversationFromGroupService;
import com.alertmns.messaging.application.CreateDirectConversationService;
import com.alertmns.messaging.application.CurrentMemberResolver;
import com.alertmns.messaging.application.ListMyConversationsService;
import com.alertmns.messaging.application.PostMessageService;
import com.alertmns.messaging.application.ReadConversationMessagesService;
import com.alertmns.messaging.application.RenameConversationService;
import com.alertmns.messaging.domain.port.incoming.CreateConversationFromGroupUseCase;
import com.alertmns.messaging.domain.port.incoming.RenameConversationUseCase;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipChecker;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.messaging.infrastructure.adapter.incoming.event.CreateConversationOnGroupCreatedListener;
import com.alertmns.messaging.infrastructure.adapter.incoming.event.RenameConversationOnGroupRenamedListener;
import com.alertmns.messaging.infrastructure.adapter.outgoing.acl.GroupMembershipPortAdapter;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationPersistenceAdapter;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.MessageJpaRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.MessagePersistenceAdapter;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
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

    @Bean
    public MessageRepository messageRepository(MessageJpaRepository jpaRepository) {
        return new MessagePersistenceAdapter(jpaRepository);
    }

    @Bean
    public GroupMembershipPort groupMembershipPort(GroupMembershipRepository groupMembershipRepository) {
        return new GroupMembershipPortAdapter(groupMembershipRepository);
    }

    // --- Services ---

    @Bean
    public CurrentMemberResolver currentMemberResolver(
            CurrentUserPort currentUserPort,
            MemberRepository memberRepository
    ) {
        return new CurrentMemberResolver(currentUserPort, memberRepository);
    }

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
            CurrentMemberResolver currentMemberResolver,
            MemberRepository memberRepository,
            ConversationRepository conversationRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new CreateDirectConversationService(
                currentMemberResolver,
                memberRepository,
                conversationRepository,
                publisher,
                clock
        );
    }

    @Bean
    public PostMessageService postMessageService(
            CurrentMemberResolver currentMemberResolver,
            ConversationRepository conversationRepository,
            GroupMembershipChecker groupMembershipChecker,
            MessageRepository messageRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new PostMessageService(
                currentMemberResolver,
                conversationRepository,
                groupMembershipChecker,
                messageRepository,
                publisher,
                clock
        );
    }

    @Bean
    public ReadConversationMessagesService readConversationMessagesService(
            CurrentMemberResolver currentMemberResolver,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            GroupMembershipChecker groupMembershipChecker
    ) {
        return new ReadConversationMessagesService(
                currentMemberResolver,
                conversationRepository,
                messageRepository,
                groupMembershipChecker
        );
    }

    @Bean
    public ListMyConversationsService listMyConversationsService(
            CurrentMemberResolver currentMemberResolver,
            ConversationRepository conversationRepository,
            GroupMembershipPort groupMembershipPort
    ) {
        return new ListMyConversationsService(currentMemberResolver, conversationRepository, groupMembershipPort);
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
