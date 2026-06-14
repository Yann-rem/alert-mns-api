package com.alertmns.messaging.infrastructure.adapter.incoming.event;

import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaEntity;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaRepository;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.port.incoming.CreateGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.RenameGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.CreateGroupCommand;
import com.alertmns.organisation.domain.port.incoming.command.RenameGroupCommand;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration de la cascade {@code GroupRenamed → Conversation} renommée (ADR-0016, propagation du nom).
 *
 * <p>Valide le câblage de bout en bout : renommer un groupe publie {@code GroupRenamed}, que le listener
 * {@link RenameConversationOnGroupRenamedListener} traduit en renommage de la conversation adossée.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GroupRenamed → Conversation rename cascade")
class RenameConversationOnGroupRenamedIntegrationTest {

    private static final String ORGANISATION_ID = "00000000-0000-0000-0000-000000000001";
    private static final String INITIAL_NAME = "Développeurs";
    private static final String NEW_NAME = "Backend";

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
    private RenameGroupUseCase renameGroupUseCase;

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
    @DisplayName("renaming a group propagates the new name to its conversation")
    void shouldRenameConversationWhenGroupIsRenamed() {
        GroupId groupId = createGroupUseCase.create(new CreateGroupCommand(ORGANISATION_ID, INITIAL_NAME));

        renameGroupUseCase.rename(new RenameGroupCommand(ORGANISATION_ID, groupId.value().toString(), NEW_NAME));

        ConversationJpaEntity conversation = conversationJpaRepository
                .findByGroupId(groupId.value())
                .orElseThrow();
        assertThat(conversation.getName()).isEqualTo(NEW_NAME);
    }
}
