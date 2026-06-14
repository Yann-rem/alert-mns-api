package com.alertmns.messaging.infrastructure.adapter.incoming.event;

import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaEntity;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaRepository;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.port.incoming.CreateGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.CreateGroupCommand;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration de la cascade {@code GroupCreated → Conversation} (ADR-0016).
 *
 * <p>Valide le câblage de bout en bout : créer un groupe via {@code CreateGroupUseCase} publie {@code GroupCreated},
 * que le listener {@link CreateConversationOnGroupCreatedListener} traduit en création de la conversation adossée.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GroupCreated → Conversation cascade")
class CreateConversationOnGroupCreatedIntegrationTest {

    private static final String ORGANISATION_ID = "00000000-0000-0000-0000-000000000001";
    private static final String GROUP_NAME = "Développeurs";

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @MockitoBean
    private MailerPort mailer;

    @Autowired
    private CreateGroupUseCase createGroupUseCase;

    @Autowired
    private ConversationJpaRepository conversationJpaRepository;

    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @AfterEach
    void cleanDatabase() {
        conversationJpaRepository.deleteAll();
        groupJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("creating a group provisions its conversation with kind GROUP, inherited name and organisation")
    void shouldProvisionConversationWhenGroupIsCreated() {
        GroupId groupId = createGroupUseCase.create(new CreateGroupCommand(ORGANISATION_ID, GROUP_NAME));

        ConversationJpaEntity conversation = conversationJpaRepository
                .findByGroupId(groupId.value())
                .orElseThrow();
        assertThat(conversation.getKind()).isEqualTo(ConversationKind.GROUP);
        assertThat(conversation.getName()).isEqualTo(GROUP_NAME);
        assertThat(conversation.getOrganisationId()).isEqualTo(UUID.fromString(ORGANISATION_ID));
    }
}
