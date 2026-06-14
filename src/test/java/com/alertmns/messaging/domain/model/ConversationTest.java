package com.alertmns.messaging.domain.model;

import com.alertmns.messaging.domain.event.ConversationCreated;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Conversation")
class ConversationTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final UUID GROUP_ID = UUID.randomUUID();
    static final ConversationName NAME = ConversationName.of("Général");
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a GROUP conversation with the given attributes")
        void shouldCreateAGroupConversation() {
            Conversation conversation = Conversation.createForGroup(ORGANISATION_ID, GROUP_ID, NAME, NOW);

            assertNotNull(conversation.id());
            assertEquals(ORGANISATION_ID, conversation.organisationId());
            assertEquals(GROUP_ID, conversation.groupId());
            assertEquals(NAME, conversation.name());
            assertEquals(ConversationKind.GROUP, conversation.kind());
            assertEquals(NOW, conversation.createdAt());
        }

        @Test
        @DisplayName("should emit ConversationCreated with kind GROUP and occurredOn = now")
        void shouldEmitConversationCreated() {
            Conversation conversation = Conversation.createForGroup(ORGANISATION_ID, GROUP_ID, NAME, NOW);

            List<DomainEvent> events = conversation.pullDomainEvents();
            assertEquals(1, events.size());
            ConversationCreated event = assertInstanceOf(ConversationCreated.class, events.getFirst());
            assertEquals(conversation.id(), event.conversationId());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(GROUP_ID, event.groupId());
            assertEquals(ConversationKind.GROUP, event.kind());
            assertEquals(NOW, event.occurredOn());
        }
    }

    @Nested
    @DisplayName("Reconstitution")
    class Reconstitution {

        @Test
        @DisplayName("should restore state without emitting any event")
        void shouldReconstituteWithoutEvents() {
            ConversationId id = ConversationId.generate();

            Conversation conversation = Conversation.reconstitute(
                    id, ORGANISATION_ID, GROUP_ID, NAME, ConversationKind.GROUP, NOW);

            assertEquals(id, conversation.id());
            assertEquals(ConversationKind.GROUP, conversation.kind());
            assertTrue(conversation.pullDomainEvents().isEmpty());
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        @Test
        @DisplayName("pullDomainEvents should clear events after pull")
        void pullDomainEventsShouldClearEventsAfterPull() {
            Conversation conversation = Conversation.createForGroup(ORGANISATION_ID, GROUP_ID, NAME, NOW);

            conversation.pullDomainEvents();

            assertTrue(conversation.pullDomainEvents().isEmpty());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two conversations with same id should be equal")
        void twoConversationsWithSameIdShouldBeEqual() {
            ConversationId id = ConversationId.generate();
            Conversation a = Conversation.reconstitute(
                    id, ORGANISATION_ID, GROUP_ID, NAME, ConversationKind.GROUP, NOW);
            Conversation b = Conversation.reconstitute(
                    id, OrganisationId.generate(), UUID.randomUUID(),
                    ConversationName.of("Autre"), ConversationKind.GROUP, NOW);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("two conversations with different ids should not be equal")
        void twoConversationsWithDifferentIdsShouldNotBeEqual() {
            Conversation a = Conversation.reconstitute(
                    ConversationId.generate(), ORGANISATION_ID, GROUP_ID, NAME, ConversationKind.GROUP, NOW);
            Conversation b = Conversation.reconstitute(
                    ConversationId.generate(), ORGANISATION_ID, GROUP_ID, NAME, ConversationKind.GROUP, NOW);

            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null organisationId")
        void shouldRejectNullOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> Conversation.createForGroup(null, GROUP_ID, NAME, NOW));
        }

        @Test
        @DisplayName("should reject null groupId")
        void shouldRejectNullGroupId() {
            assertThrows(NullPointerException.class,
                    () -> Conversation.createForGroup(ORGANISATION_ID, null, NAME, NOW));
        }

        @Test
        @DisplayName("should reject null name")
        void shouldRejectNullName() {
            assertThrows(NullPointerException.class,
                    () -> Conversation.createForGroup(ORGANISATION_ID, GROUP_ID, null, NOW));
        }

        @Test
        @DisplayName("should reject null now")
        void shouldRejectNullNow() {
            assertThrows(NullPointerException.class,
                    () -> Conversation.createForGroup(ORGANISATION_ID, GROUP_ID, NAME, null));
        }
    }
}
