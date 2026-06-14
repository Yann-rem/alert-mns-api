package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.port.incoming.CreateConversationFromGroupUseCase;
import com.alertmns.messaging.domain.port.incoming.command.CreateConversationFromGroupCommand;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * Service applicatif orchestrant la création de la conversation d'un groupe.
 *
 * <p>Parse (VOs) → check (existence) → act (Conversation.createForGroup) → save → publish.
 * Idempotent : no-op si une conversation existe déjà pour ce groupe.</p>
 */
public final class CreateConversationFromGroupService implements CreateConversationFromGroupUseCase {

    private final ConversationRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public CreateConversationFromGroupService(
            ConversationRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void create(CreateConversationFromGroupCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        UUID groupId = UUID.fromString(command.groupId());

        if (repository.existsByGroupId(groupId)) {
            return;
        }

        ConversationName name = ConversationName.of(command.name());
        Conversation conversation = Conversation.createForGroup(organisationId, groupId, name, clock.instant());
        repository.save(conversation);
        publisher.publish(conversation.pullDomainEvents());
    }
}
