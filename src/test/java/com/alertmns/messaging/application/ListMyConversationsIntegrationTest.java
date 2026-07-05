package com.alertmns.messaging.application;

import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.ListMyConversationsUseCase;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test d'intégration du listage des conversations de l'utilisateur courant via {@link ListMyConversationsUseCase}.
 *
 * <p>Vérifie, sur une base réelle, qu'on retourne ses DM et les conversations de ses groupes (via l'ACL), triées du
 * plus récent au plus ancien, et qu'on exclut les conversations dont il ne fait pas partie.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("List my conversations (use case integration)")
class ListMyConversationsIntegrationTest {

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
    private ListMyConversationsUseCase listMyConversationsUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private ConversationJpaRepository conversationJpaRepository;

    @Autowired
    private GroupMembershipJpaRepository groupMembershipJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    private MemberId memberId;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        memberId = MemberId.generate();
        memberRepository.save(Member.reconstitute(
                memberId, ORGANISATION_ID, userId, MemberRole.MEMBER, MemberStatus.ACTIVE, NOW));
        when(currentUserPort.currentUser())
                .thenReturn(Optional.of(new AuthenticatedUser(UserId.from(userId))));
    }

    @AfterEach
    void cleanDatabase() {
        conversationJpaRepository.deleteAll();
        groupMembershipJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("returns the member's DM and group conversations, most recent first, excluding others")
    void shouldReturnMyConversations() {
        Conversation myDm = Conversation.reconstitute(
                ConversationId.generate(), ORGANISATION_ID, null, null, ConversationKind.DIRECT,
                ParticipantPair.of(memberId.value(), UUID.randomUUID()), NOW);
        conversationRepository.save(myDm);

        UUID groupId = UUID.randomUUID();
        Conversation myGroup = Conversation.reconstitute(
                ConversationId.generate(), ORGANISATION_ID, groupId, ConversationName.of("Général"),
                ConversationKind.GROUP, null, NOW.plusSeconds(120));
        conversationRepository.save(myGroup);
        groupMembershipRepository.save(GroupMembership.reconstitute(
                GroupMembershipId.from(UUID.randomUUID()), ORGANISATION_ID, GroupId.from(groupId), memberId, NOW));

        // a DM between two other members → must be excluded
        conversationRepository.save(Conversation.reconstitute(
                ConversationId.generate(), ORGANISATION_ID, null, null, ConversationKind.DIRECT,
                ParticipantPair.of(UUID.randomUUID(), UUID.randomUUID()), NOW));
        // a group the member is not in → must be excluded
        conversationRepository.save(Conversation.reconstitute(
                ConversationId.generate(), ORGANISATION_ID, UUID.randomUUID(), ConversationName.of("Autre"),
                ConversationKind.GROUP, null, NOW));

        List<Conversation> result = listMyConversationsUseCase.list();

        assertThat(result).extracting(Conversation::id).containsExactly(myGroup.id(), myDm.id());
    }

    @Test
    @DisplayName("returns an empty list when the member has no conversation")
    void shouldReturnEmptyWhenNone() {
        assertThat(listMyConversationsUseCase.list()).isEmpty();
    }
}
