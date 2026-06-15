package com.alertmns.messaging.domain.model;

import com.alertmns.messaging.domain.event.ConversationCreated;
import com.alertmns.messaging.domain.event.ConversationRenamed;
import com.alertmns.messaging.domain.event.DirectConversationCreated;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Conversation")
class ConversationTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final UUID GROUP_ID = UUID.randomUUID();
    static final UUID MEMBER_A = UUID.randomUUID();
    static final UUID MEMBER_B = UUID.randomUUID();
    static final ConversationName NAME = ConversationName.of("Général");
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");
    static final Instant LATER = NOW.plusSeconds(60);

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
    @DisplayName("Direct creation")
    class DirectCreation {

        @Test
        @DisplayName("should create a DIRECT conversation with canonically ordered participants and no group/name")
        void shouldCreateADirectConversation() {
            Conversation conversation = Conversation.createDirect(ORGANISATION_ID, MEMBER_A, MEMBER_B, NOW);

            assertNotNull(conversation.id());
            assertEquals(ORGANISATION_ID, conversation.organisationId());
            assertEquals(ConversationKind.DIRECT, conversation.kind());
            assertNull(conversation.groupId());
            assertNull(conversation.name());
            assertEquals(NOW, conversation.createdAt());

            UUID expectedLow = MEMBER_A.compareTo(MEMBER_B) < 0 ? MEMBER_A : MEMBER_B;
            UUID expectedHigh = expectedLow == MEMBER_A ? MEMBER_B : MEMBER_A;
            assertEquals(expectedLow, conversation.participantLow());
            assertEquals(expectedHigh, conversation.participantHigh());
            assertTrue(conversation.participantLow().compareTo(conversation.participantHigh()) < 0);
        }

        @Test
        @DisplayName("should emit DirectConversationCreated with the participants and occurredOn = now")
        void shouldEmitDirectConversationCreated() {
            Conversation conversation = Conversation.createDirect(ORGANISATION_ID, MEMBER_A, MEMBER_B, NOW);

            List<DomainEvent> events = conversation.pullDomainEvents();
            assertEquals(1, events.size());
            DirectConversationCreated event = assertInstanceOf(DirectConversationCreated.class, events.getFirst());
            assertEquals(conversation.id(), event.conversationId());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(conversation.participantLow(), event.participantLow());
            assertEquals(conversation.participantHigh(), event.participantHigh());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("participant order is canonical regardless of argument order")
        void participantOrderIsCanonical() {
            Conversation ab = Conversation.createDirect(ORGANISATION_ID, MEMBER_A, MEMBER_B, NOW);
            Conversation ba = Conversation.createDirect(ORGANISATION_ID, MEMBER_B, MEMBER_A, NOW);

            assertEquals(ab.participantLow(), ba.participantLow());
            assertEquals(ab.participantHigh(), ba.participantHigh());
        }

        @Test
        @DisplayName("should reject two identical members")
        void shouldRejectIdenticalMembers() {
            assertThrows(IllegalArgumentException.class,
                    () -> Conversation.createDirect(ORGANISATION_ID, MEMBER_A, MEMBER_A, NOW));
        }

        @Test
        @DisplayName("should reject null members")
        void shouldRejectNullMembers() {
            assertThrows(NullPointerException.class,
                    () -> Conversation.createDirect(ORGANISATION_ID, null, MEMBER_B, NOW));
            assertThrows(NullPointerException.class,
                    () -> Conversation.createDirect(ORGANISATION_ID, MEMBER_A, null, NOW));
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
                    id, ORGANISATION_ID, GROUP_ID, NAME, ConversationKind.GROUP, null, null, NOW);

            assertEquals(id, conversation.id());
            assertEquals(ConversationKind.GROUP, conversation.kind());
            assertTrue(conversation.pullDomainEvents().isEmpty());
        }
    }

    @Nested
    @DisplayName("Renaming")
    class Renaming {

        @Test
        @DisplayName("rename should change the name and emit ConversationRenamed")
        void renameShouldChangeNameAndEmitEvent() {
            Conversation conversation = Conversation.createForGroup(ORGANISATION_ID, GROUP_ID, NAME, NOW);
            conversation.pullDomainEvents();

            ConversationName newName = ConversationName.of("Backend");
            conversation.rename(newName, LATER);

            assertEquals(newName, conversation.name());
            List<DomainEvent> events = conversation.pullDomainEvents();
            assertEquals(1, events.size());
            ConversationRenamed event = assertInstanceOf(ConversationRenamed.class, events.getFirst());
            assertEquals(conversation.id(), event.conversationId());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(newName, event.name());
            assertEquals(LATER, event.occurredOn());
        }

        @Test
        @DisplayName("rename to the same name should be a no-op without event")
        void renameToSameNameShouldBeNoOp() {
            Conversation conversation = Conversation.createForGroup(ORGANISATION_ID, GROUP_ID, NAME, NOW);
            conversation.pullDomainEvents();

            conversation.rename(NAME, LATER);

            assertEquals(NAME, conversation.name());
            assertTrue(conversation.pullDomainEvents().isEmpty());
        }

        @Test
        @DisplayName("rename should reject null name")
        void renameShouldRejectNullName() {
            Conversation conversation = Conversation.createForGroup(ORGANISATION_ID, GROUP_ID, NAME, NOW);

            assertThrows(NullPointerException.class, () -> conversation.rename(null, LATER));
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
                    id, ORGANISATION_ID, GROUP_ID, NAME, ConversationKind.GROUP, null, null, NOW);
            Conversation b = Conversation.reconstitute(
                    id, OrganisationId.generate(), UUID.randomUUID(),
                    ConversationName.of("Autre"), ConversationKind.GROUP, null, null, NOW);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("two conversations with different ids should not be equal")
        void twoConversationsWithDifferentIdsShouldNotBeEqual() {
            Conversation a = Conversation.reconstitute(
                    ConversationId.generate(), ORGANISATION_ID, GROUP_ID, NAME, ConversationKind.GROUP, null, null, NOW);
            Conversation b = Conversation.reconstitute(
                    ConversationId.generate(), ORGANISATION_ID, GROUP_ID, NAME, ConversationKind.GROUP, null, null, NOW);

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
