package com.alertmns.messaging.infrastructure.config;

import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.messaging.application.CreateConversationFromGroupService;
import com.alertmns.messaging.application.CreateDirectConversationService;
import com.alertmns.messaging.application.DispatchMessageService;
import com.alertmns.messaging.application.ListMyConversationsService;
import com.alertmns.messaging.application.MemberNameResolver;
import com.alertmns.messaging.application.PostMessageService;
import com.alertmns.messaging.application.ReadConversationMessagesService;
import com.alertmns.messaging.application.RenameConversationService;
import com.alertmns.messaging.domain.port.incoming.CreateConversationFromGroupUseCase;
import com.alertmns.messaging.domain.port.incoming.RenameConversationUseCase;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipChecker;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.messaging.domain.port.outgoing.MemberDirectoryPort;
import com.alertmns.messaging.domain.port.outgoing.MessageRealtimePort;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.messaging.domain.port.outgoing.UserDirectoryPort;
import com.alertmns.messaging.infrastructure.adapter.incoming.event.CreateConversationOnGroupCreatedListener;
import com.alertmns.messaging.infrastructure.adapter.incoming.event.PushMessageOnPostedListener;
import com.alertmns.messaging.infrastructure.adapter.incoming.event.RenameConversationOnGroupRenamedListener;
import com.alertmns.messaging.infrastructure.adapter.outgoing.acl.GroupMembershipPortAdapter;
import com.alertmns.messaging.infrastructure.adapter.outgoing.acl.MemberDirectoryPortAdapter;
import com.alertmns.messaging.infrastructure.adapter.outgoing.acl.UserDirectoryPortAdapter;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationPersistenceAdapter;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.MessageJpaRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.MessagePersistenceAdapter;
import com.alertmns.messaging.infrastructure.adapter.outgoing.realtime.MessageRealtimeAdapter;
import com.alertmns.organisation.application.CurrentMemberResolver;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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

    @Bean
    public MemberDirectoryPort memberDirectoryPort(MemberRepository memberRepository) {
        return new MemberDirectoryPortAdapter(memberRepository);
    }

    @Bean
    public UserDirectoryPort userDirectoryPort(UserRepository userRepository) {
        return new UserDirectoryPortAdapter(userRepository);
    }

    @Bean
    public MessageRealtimePort messageRealtimePort(SimpMessagingTemplate messagingTemplate) {
        return new MessageRealtimeAdapter(messagingTemplate);
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

    /**
     * Résolution du nom d'un membre, partagée par le push temps réel et les deux services de lecture :
     * le même chemin pour tous, sinon un membre anonymisé s'afficherait différemment selon la voie.
     */
    @Bean
    public MemberNameResolver memberNameResolver(
            MemberDirectoryPort memberDirectoryPort,
            UserDirectoryPort userDirectoryPort
    ) {
        return new MemberNameResolver(memberDirectoryPort, userDirectoryPort);
    }

    @Bean
    public ReadConversationMessagesService readConversationMessagesService(
            CurrentMemberResolver currentMemberResolver,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            GroupMembershipChecker groupMembershipChecker,
            MemberNameResolver memberNameResolver
    ) {
        return new ReadConversationMessagesService(
                currentMemberResolver,
                conversationRepository,
                messageRepository,
                groupMembershipChecker,
                memberNameResolver
        );
    }

    @Bean
    public ListMyConversationsService listMyConversationsService(
            CurrentMemberResolver currentMemberResolver,
            ConversationRepository conversationRepository,
            GroupMembershipPort groupMembershipPort,
            MessageRepository messageRepository,
            MemberNameResolver memberNameResolver
    ) {
        return new ListMyConversationsService(
                currentMemberResolver,
                conversationRepository,
                groupMembershipPort,
                messageRepository,
                memberNameResolver
        );
    }

    @Bean
    public DispatchMessageService dispatchMessageService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            MemberDirectoryPort memberDirectoryPort,
            MemberNameResolver memberNameResolver,
            MessageRealtimePort messageRealtimePort
    ) {
        return new DispatchMessageService(
                conversationRepository,
                messageRepository,
                memberDirectoryPort,
                memberNameResolver,
                messageRealtimePort
        );
    }

    // --- Event listeners ---

    @Bean
    public CreateConversationOnGroupCreatedListener createConversationOnGroupCreatedListener(
            CreateConversationFromGroupUseCase createConversationFromGroupUseCase) {
        return new CreateConversationOnGroupCreatedListener(createConversationFromGroupUseCase);
    }

    @Bean
    public PushMessageOnPostedListener pushMessageOnPostedListener(DispatchMessageService dispatchMessageService) {
        return new PushMessageOnPostedListener(dispatchMessageService);
    }

    @Bean
    public RenameConversationOnGroupRenamedListener renameConversationOnGroupRenamedListener(
            RenameConversationUseCase renameConversationUseCase) {
        return new RenameConversationOnGroupRenamedListener(renameConversationUseCase);
    }
}
