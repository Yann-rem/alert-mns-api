package com.alertmns.messaging.application;

import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.messaging.domain.exception.NotAConversationParticipantException;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageContent;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.PostMessageUseCase;
import com.alertmns.messaging.domain.port.incoming.command.PostMessageCommand;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.MessageJpaEntity;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.MessageJpaRepository;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.GroupMembershipId;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupMembershipJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberJpaRepository;
import com.alertmns.shared.AuthenticatedUser;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Test d'intégration de l'envoi d'un message via {@link PostMessageUseCase}.
 *
 * <p>Exerce de bout en bout, sur une base réelle : la résolution de l'auteur (current user mocké → Member), le
 * contrôle d'accès (paire d'un DM, et appartenance au groupe via l'adapter ACL réel), la persistance du message
 * (mapper {@code toEntity}) et la validation de la cible d'une réponse.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Post message (use case integration)")
class PostMessageIntegrationTest {

    private static final OrganisationId ORGANISATION_ID =
            OrganisationId.from(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final Instant NOW = Instant.parse("2026-06-01T10:00:00Z");

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @MockitoBean
    private MailerPort mailer;

    @MockitoBean
    private CurrentUserPort currentUserPort;

    @Autowired
    private PostMessageUseCase postMessageUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private ConversationJpaRepository conversationJpaRepository;

    @Autowired
    private MessageJpaRepository messageJpaRepository;

    @Autowired
    private GroupMembershipJpaRepository groupMembershipJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    private MemberId authorId;

    @BeforeEach
    void setUp() {
        UUID authorUserId = UUID.randomUUID();
        authorId = MemberId.generate();
        memberRepository.save(Member.reconstitute(
                authorId, ORGANISATION_ID, authorUserId, MemberRole.MEMBER, MemberStatus.ACTIVE, NOW));
        when(currentUserPort.currentUser())
                .thenReturn(Optional.of(new AuthenticatedUser(UserId.from(authorUserId))));
    }

    @AfterEach
    void cleanDatabase() {
        messageJpaRepository.deleteAll();
        conversationJpaRepository.deleteAll();
        groupMembershipJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    private Conversation directConversationWithAuthor() {
        ParticipantPair pair = ParticipantPair.of(authorId.value(), MemberId.generate().value());
        return Conversation.reconstitute(
                ConversationId.generate(), ORGANISATION_ID, null, null, ConversationKind.DIRECT, pair, NOW);
    }

    private Conversation groupConversation(UUID groupId, String name) {
        return Conversation.reconstitute(
                ConversationId.generate(), ORGANISATION_ID, groupId, ConversationName.of(name),
                ConversationKind.GROUP, null, NOW);
    }

    @Test
    @DisplayName("posts a root message in a DIRECT conversation the author belongs to")
    void shouldPostRootMessageInDirectConversation() {
        Conversation conversation = directConversationWithAuthor();
        conversationRepository.save(conversation);

        MessageId id = postMessageUseCase.post(
                new PostMessageCommand(conversation.id().value().toString(), "Bonjour", null));

        MessageJpaEntity saved = messageJpaRepository.findById(id.value()).orElseThrow();
        assertThat(saved.getConversationId()).isEqualTo(conversation.id().value());
        assertThat(saved.getAuthorId()).isEqualTo(authorId.value());
        assertThat(saved.getContent()).isEqualTo("Bonjour");
        assertThat(saved.getReplyTo()).isNull();
        assertThat(saved.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("posts a reply referencing a message of the same conversation")
    void shouldPostReply() {
        Conversation conversation = directConversationWithAuthor();
        conversationRepository.save(conversation);
        Message target = Message.reconstitute(
                MessageId.generate(), conversation.id(), authorId.value(),
                MessageContent.of("Message d'origine"), null, NOW);
        messageRepository.save(target);

        MessageId id = postMessageUseCase.post(new PostMessageCommand(
                conversation.id().value().toString(), "Réponse", target.id().value().toString()));

        MessageJpaEntity saved = messageJpaRepository.findById(id.value()).orElseThrow();
        assertThat(saved.getReplyTo()).isEqualTo(target.id().value());
    }

    @Test
    @DisplayName("posts in a GROUP conversation when the author is a member of the group")
    void shouldPostInGroupWhenMember() {
        UUID groupId = UUID.randomUUID();
        Conversation conversation = groupConversation(groupId, "Général");
        conversationRepository.save(conversation);
        groupMembershipRepository.save(GroupMembership.reconstitute(
                GroupMembershipId.from(UUID.randomUUID()), ORGANISATION_ID, GroupId.from(groupId), authorId, NOW));

        MessageId id = postMessageUseCase.post(
                new PostMessageCommand(conversation.id().value().toString(), "Salut le groupe", null));

        assertThat(messageJpaRepository.findById(id.value())).isPresent();
    }

    @Test
    @DisplayName("rejects posting in a GROUP conversation when the author is not a member of the group")
    void shouldRejectWhenNotGroupMember() {
        UUID groupId = UUID.randomUUID();
        Conversation conversation = groupConversation(groupId, "Privé");
        conversationRepository.save(conversation);

        assertThatThrownBy(() -> postMessageUseCase.post(
                new PostMessageCommand(conversation.id().value().toString(), "Intrus", null)))
                .isInstanceOf(NotAConversationParticipantException.class);

        assertThat(messageJpaRepository.findAll()).isEmpty();
    }
}
