package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.event.ConversationRenamed;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.port.incoming.command.RenameConversationCommand;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RenameConversationService")
@ExtendWith(MockitoExtension.class)
class RenameConversationServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final UUID GROUP_ID = UUID.randomUUID();
    static final ConversationName OLD_NAME = ConversationName.of("Général");
    static final String NEW_NAME = "Backend";
    static final Instant CREATED_AT = Instant.parse("2026-05-01T09:00:00Z");
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    ConversationRepository repository;

    @Mock
    EventPublisher publisher;

    @Mock
    Clock clock;

    @InjectMocks
    RenameConversationService service;

    @BeforeEach
    void stubClock() {
        lenient().when(clock.instant()).thenReturn(NOW);
    }

    private RenameConversationCommand command() {
        return new RenameConversationCommand(GROUP_ID.toString(), NEW_NAME);
    }

    private Conversation existingConversation(ConversationId id) {
        return Conversation.reconstitute(
                id, ORGANISATION_ID, GROUP_ID, OLD_NAME, ConversationKind.GROUP, null, CREATED_AT);
    }

    @Nested
    @DisplayName("Renaming")
    class Renaming {

        @Test
        @DisplayName("should rename the conversation and save it when one exists for the group")
        void shouldRenameWhenConversationExists() {
            Conversation conversation = existingConversation(ConversationId.generate());
            when(repository.findByGroupId(GROUP_ID)).thenReturn(Optional.of(conversation));

            service.rename(command());

            verify(repository).save(conversation);
            assertEquals(ConversationName.of(NEW_NAME), conversation.name());
        }

        @Test
        @DisplayName("should publish ConversationRenamed with the new name and occurredOn = now")
        void shouldPublishConversationRenamed() {
            ConversationId id = ConversationId.generate();
            when(repository.findByGroupId(GROUP_ID)).thenReturn(Optional.of(existingConversation(id)));

            service.rename(command());

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            ConversationRenamed event = assertInstanceOf(ConversationRenamed.class, events.getFirst());
            assertEquals(id, event.conversationId());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(ConversationName.of(NEW_NAME), event.name());
            assertEquals(NOW, event.occurredOn());
        }
    }

    @Nested
    @DisplayName("Tolerance")
    class Tolerance {

        @Test
        @DisplayName("should be a no-op when no conversation exists for the group")
        void shouldBeNoOpWhenConversationAbsent() {
            when(repository.findByGroupId(GROUP_ID)).thenReturn(Optional.empty());

            service.rename(command());

            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should throw IllegalArgumentException when groupId is not a valid UUID")
        void shouldThrowWhenGroupIdInvalid() {
            RenameConversationCommand command = new RenameConversationCommand("invalid", NEW_NAME);

            assertThrows(IllegalArgumentException.class, () -> service.rename(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should reject null repository")
        void shouldRejectNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new RenameConversationService(null, publisher, clock));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new RenameConversationService(repository, null, clock));
        }

        @Test
        @DisplayName("should reject null clock")
        void shouldRejectNullClock() {
            assertThrows(NullPointerException.class,
                    () -> new RenameConversationService(repository, publisher, null));
        }

        @Test
        @DisplayName("should reject null command groupId")
        void shouldRejectNullCommandGroupId() {
            assertThrows(NullPointerException.class,
                    () -> new RenameConversationCommand(null, NEW_NAME));
        }

        @Test
        @DisplayName("should reject null command name")
        void shouldRejectNullCommandName() {
            assertThrows(NullPointerException.class,
                    () -> new RenameConversationCommand(GROUP_ID.toString(), null));
        }
    }
}
