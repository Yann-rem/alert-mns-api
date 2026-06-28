package com.alertmns.messaging.domain.model;

import com.alertmns.messaging.domain.event.MessagePosted;
import com.alertmns.shared.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Message")
class MessageTest {

    static final ConversationId CONVERSATION_ID = ConversationId.generate();
    static final UUID AUTHOR_ID = UUID.randomUUID();
    static final MessageContent CONTENT = MessageContent.of("Bonjour");
    static final MessageId REPLY_TO = MessageId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Nested
    @DisplayName("Posting")
    class Posting {

        @Test
        @DisplayName("should create a root message with the given attributes and no replyTo")
        void shouldCreateARootMessage() {
            Message message = Message.post(CONVERSATION_ID, AUTHOR_ID, CONTENT, null, NOW);

            assertNotNull(message.id());
            assertEquals(CONVERSATION_ID, message.conversationId());
            assertEquals(AUTHOR_ID, message.authorId());
            assertEquals(CONTENT, message.content());
            assertNull(message.replyTo());
            assertEquals(NOW, message.sentAt());
        }

        @Test
        @DisplayName("should create a reply message referencing another message")
        void shouldCreateAReplyMessage() {
            Message message = Message.post(CONVERSATION_ID, AUTHOR_ID, CONTENT, REPLY_TO, NOW);

            assertEquals(REPLY_TO, message.replyTo());
        }

        @Test
        @DisplayName("should emit MessagePosted with occurredOn = now")
        void shouldEmitMessagePosted() {
            Message message = Message.post(CONVERSATION_ID, AUTHOR_ID, CONTENT, null, NOW);

            List<DomainEvent> events = message.pullDomainEvents();
            assertEquals(1, events.size());
            MessagePosted event = assertInstanceOf(MessagePosted.class, events.getFirst());
            assertEquals(message.id(), event.messageId());
            assertEquals(CONVERSATION_ID, event.conversationId());
            assertEquals(AUTHOR_ID, event.authorId());
            assertEquals(NOW, event.occurredOn());
        }
    }

    @Nested
    @DisplayName("Reconstitution")
    class Reconstitution {

        @Test
        @DisplayName("should restore state without emitting any event")
        void shouldReconstituteWithoutEvents() {
            MessageId id = MessageId.generate();

            Message message = Message.reconstitute(id, CONVERSATION_ID, AUTHOR_ID, CONTENT, REPLY_TO, NOW);

            assertEquals(id, message.id());
            assertEquals(REPLY_TO, message.replyTo());
            assertTrue(message.pullDomainEvents().isEmpty());
        }
    }

    @Nested
    @DisplayName("Belonging")
    class Belonging {

        @Test
        @DisplayName("belongsTo should return true for its own conversation")
        void belongsToOwnConversation() {
            Message message = Message.post(CONVERSATION_ID, AUTHOR_ID, CONTENT, null, NOW);

            assertTrue(message.belongsTo(CONVERSATION_ID));
        }

        @Test
        @DisplayName("belongsTo should return false for another conversation")
        void belongsToAnotherConversation() {
            Message message = Message.post(CONVERSATION_ID, AUTHOR_ID, CONTENT, null, NOW);

            assertFalse(message.belongsTo(ConversationId.generate()));
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        @Test
        @DisplayName("pullDomainEvents should clear events after pull")
        void pullDomainEventsShouldClearEventsAfterPull() {
            Message message = Message.post(CONVERSATION_ID, AUTHOR_ID, CONTENT, null, NOW);

            message.pullDomainEvents();

            assertTrue(message.pullDomainEvents().isEmpty());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two messages with same id should be equal")
        void twoMessagesWithSameIdShouldBeEqual() {
            MessageId id = MessageId.generate();
            Message a = Message.reconstitute(id, CONVERSATION_ID, AUTHOR_ID, CONTENT, null, NOW);
            Message b = Message.reconstitute(
                    id, ConversationId.generate(), UUID.randomUUID(), MessageContent.of("Autre"), null, NOW);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("two messages with different ids should not be equal")
        void twoMessagesWithDifferentIdsShouldNotBeEqual() {
            Message a = Message.reconstitute(MessageId.generate(), CONVERSATION_ID, AUTHOR_ID, CONTENT, null, NOW);
            Message b = Message.reconstitute(MessageId.generate(), CONVERSATION_ID, AUTHOR_ID, CONTENT, null, NOW);

            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null conversationId")
        void shouldRejectNullConversationId() {
            assertThrows(NullPointerException.class,
                    () -> Message.post(null, AUTHOR_ID, CONTENT, null, NOW));
        }

        @Test
        @DisplayName("should reject null authorId")
        void shouldRejectNullAuthorId() {
            assertThrows(NullPointerException.class,
                    () -> Message.post(CONVERSATION_ID, null, CONTENT, null, NOW));
        }

        @Test
        @DisplayName("should reject null content")
        void shouldRejectNullContent() {
            assertThrows(NullPointerException.class,
                    () -> Message.post(CONVERSATION_ID, AUTHOR_ID, null, null, NOW));
        }

        @Test
        @DisplayName("should reject null now")
        void shouldRejectNullNow() {
            assertThrows(NullPointerException.class,
                    () -> Message.post(CONVERSATION_ID, AUTHOR_ID, CONTENT, null, null));
        }
    }
}
