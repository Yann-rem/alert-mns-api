package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.port.incoming.RenameConversationUseCase;
import com.alertmns.messaging.domain.port.incoming.command.RenameConversationCommand;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.shared.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service applicatif orchestrant le renommage de la conversation d'un groupe.
 *
 * <p>Parse (VOs) → load (par groupId) → act (Conversation.rename) → save → publish. Tolérant : no-op avec log si
 * aucune conversation n'existe pour le groupe.</p>
 */
public final class RenameConversationService implements RenameConversationUseCase {

    private static final Logger log = LoggerFactory.getLogger(RenameConversationService.class);

    private final ConversationRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public RenameConversationService(ConversationRepository repository, EventPublisher publisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void rename(RenameConversationCommand command) {
        UUID groupId = UUID.fromString(command.groupId());
        ConversationName name = ConversationName.of(command.name());

        Optional<Conversation> found = repository.findByGroupId(groupId);
        if (found.isEmpty()) {
            log.warn("No conversation found for group {} — skipping rename propagation", groupId);
            return;
        }

        Conversation conversation = found.get();
        conversation.rename(name, clock.instant());
        repository.save(conversation);
        publisher.publish(conversation.pullDomainEvents());
    }
}
