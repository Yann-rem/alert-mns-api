package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.event.ConversationCreated;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.port.incoming.command.CreateConversationFromGroupCommand;
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

@DisplayName("CreateConversationFromGroupService")
@ExtendWith(MockitoExtension.class)
class CreateConversationFromGroupServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final UUID GROUP_ID = UUID.randomUUID();
    static final String NAME = "Général";
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    ConversationRepository repository;

    @Mock
    EventPublisher publisher;

    @Mock
    Clock clock;

    @InjectMocks
    CreateConversationFromGroupService service;

    @BeforeEach
    void stubClock() {
        lenient().when(clock.instant()).thenReturn(NOW);
    }

    private CreateConversationFromGroupCommand command() {
        return new CreateConversationFromGroupCommand(
                ORGANISATION_ID.value().toString(), GROUP_ID.toString(), NAME);
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create and save a GROUP conversation when none exists for the group")
        void shouldCreateConversationWhenAbsent() {
            when(repository.existsByGroupId(GROUP_ID)).thenReturn(false);

            service.create(command());

            ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
            verify(repository).save(conversationCaptor.capture());
            Conversation saved = conversationCaptor.getValue();
            assertEquals(ORGANISATION_ID, saved.organisationId());
            assertEquals(GROUP_ID, saved.groupId());
            assertEquals(ConversationName.of(NAME), saved.name());
            assertEquals(ConversationKind.GROUP, saved.kind());
        }

        @Test
        @DisplayName("should publish ConversationCreated with kind GROUP and occurredOn = now")
        void shouldPublishConversationCreated() {
            when(repository.existsByGroupId(GROUP_ID)).thenReturn(false);

            service.create(command());

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            ConversationCreated event = assertInstanceOf(ConversationCreated.class, events.getFirst());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(GROUP_ID, event.groupId());
            assertEquals(ConversationKind.GROUP, event.kind());
            assertEquals(NOW, event.occurredOn());
        }
    }

    @Nested
    @DisplayName("Idempotence")
    class Idempotence {

        @Test
        @DisplayName("should be a no-op when a conversation already exists for the group")
        void shouldBeNoOpWhenConversationExists() {
            when(repository.existsByGroupId(GROUP_ID)).thenReturn(true);

            service.create(command());

            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should throw IllegalArgumentException when organisationId is not a valid UUID")
        void shouldThrowWhenOrganisationIdInvalid() {
            CreateConversationFromGroupCommand command =
                    new CreateConversationFromGroupCommand("invalid", GROUP_ID.toString(), NAME);

            assertThrows(IllegalArgumentException.class, () -> service.create(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when groupId is not a valid UUID")
        void shouldThrowWhenGroupIdInvalid() {
            CreateConversationFromGroupCommand command =
                    new CreateConversationFromGroupCommand(ORGANISATION_ID.value().toString(), "invalid", NAME);

            assertThrows(IllegalArgumentException.class, () -> service.create(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should reject null repository")
        void shouldRejectNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new CreateConversationFromGroupService(null, publisher, clock));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new CreateConversationFromGroupService(repository, null, clock));
        }

        @Test
        @DisplayName("should reject null clock")
        void shouldRejectNullClock() {
            assertThrows(NullPointerException.class,
                    () -> new CreateConversationFromGroupService(repository, publisher, null));
        }

        @Test
        @DisplayName("should reject null command organisationId")
        void shouldRejectNullCommandOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> new CreateConversationFromGroupCommand(null, GROUP_ID.toString(), NAME));
        }

        @Test
        @DisplayName("should reject null command groupId")
        void shouldRejectNullCommandGroupId() {
            assertThrows(NullPointerException.class,
                    () -> new CreateConversationFromGroupCommand(ORGANISATION_ID.value().toString(), null, NAME));
        }

        @Test
        @DisplayName("should reject null command name")
        void shouldRejectNullCommandName() {
            assertThrows(NullPointerException.class,
                    () -> new CreateConversationFromGroupCommand(
                            ORGANISATION_ID.value().toString(), GROUP_ID.toString(), null));
        }
    }
}
